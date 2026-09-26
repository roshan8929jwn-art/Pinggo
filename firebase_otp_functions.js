/**
 * Firebase Cloud Functions implementation for Pinggo Email OTP Verification.
 * 
 * To deploy:
 * 1. Install Firebase CLI: npm install -g firebase-tools
 * 2. Run: firebase init functions
 * 3. Copy this code to functions/index.js
 * 4. Configure email provider (e.g. Gmail) credentials in environment:
 *    firebase functions:secrets:set EMAIL_USER
 *    firebase functions:secrets:set EMAIL_PASS
 * 5. Run: firebase deploy --only functions
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');
const crypto = require('crypto');

admin.initializeApp();

/**
 * Generates a 6-digit numeric OTP and sends it to the user's email.
 * This is a secure HTTPS Callable function.
 */
exports.sendOtp = functions.https.onCall(async (data, context) => {
    const email = data.email;
    if (!email || !email.includes('@')) {
        throw new functions.https.HttpsError('invalid-argument', 'Valid email is required.');
    }

    // Generate a secure 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    
    // Hash the OTP for secure storage
    const hashedOtp = crypto.createHash('sha256').update(otp).digest('hex');
    
    const db = admin.firestore();
    const expiresAt = Date.now() + 300000; // 5 minutes from now

    // Store the challenge securely in Firestore
    // Access is restricted via Firestore Rules (ensure rules are configured to deny client read/write to this collection)
    await db.collection('otp_challenges').document(email).set({
        hashedOtp: hashedOtp,
        expiresAt: expiresAt,
        attempts: 0
    });

    // Send the email
    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.EMAIL_USER,
            pass: process.env.EMAIL_PASS
        }
    });

    const mailOptions = {
        from: '"Pinggo Messenger" <no-reply@pinggo.app>',
        to: email,
        subject: 'Your Pinggo Verification Code',
        html: `
            <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                <h2 style="color: #FF69B4;">Welcome to Pinggo! 🐧</h2>
                <p>Use the following code to verify your email and complete your profile setup:</p>
                <div style="background: #F8F8FA; padding: 20px; text-align: center; border-radius: 12px; margin: 20px 0;">
                    <h1 style="letter-spacing: 10px; font-size: 36px; margin: 0; color: #1C1C1E;">${otp}</h1>
                </div>
                <p>This code will expire in 5 minutes.</p>
                <p>If you didn't request this code, you can safely ignore this email.</p>
                <hr style="border: 0; border-top: 1px solid #EEEEEE; margin: 20px 0;" />
                <p style="font-size: 12px; color: #8E8E93;">Pinggo — Same Vibes, New Experience</p>
            </div>
        `
    };

    try {
        await transporter.sendMail(mailOptions);
        return { success: true, message: 'OTP sent successfully' };
    } catch (error) {
        console.error('Error sending email:', error);
        throw new functions.https.HttpsError('internal', 'Failed to send verification email.');
    }
});

/**
 * Verifies the OTP provided by the user.
 * This is a secure HTTPS Callable function.
 */
exports.verifyOtp = functions.https.onCall(async (data, context) => {
    const email = data.email;
    const otp = data.otp;

    if (!email || !otp) {
        throw new functions.https.HttpsError('invalid-argument', 'Email and OTP are required.');
    }

    const db = admin.firestore();
    const docRef = db.collection('otp_challenges').document(email);
    const doc = await docRef.get();

    if (!doc.exists) {
        throw new functions.https.HttpsError('not-found', 'No verification request found for this email.');
    }

    const challenge = doc.data();
    
    // Rate limiting: check attempts
    if (challenge.attempts >= 5) {
        throw new functions.https.HttpsError('resource-exhausted', 'Too many incorrect attempts. Please request a new code.');
    }

    // Check expiration
    if (Date.now() > challenge.expiresAt) {
        await docRef.delete();
        throw new functions.https.HttpsError('deadline-exceeded', 'OTP has expired. Please request a new code.');
    }

    // Verify hashed OTP
    const inputHash = crypto.createHash('sha256').update(otp).digest('hex');
    if (inputHash === challenge.hashedOtp) {
        // Success: Mark email as verified in the user's profile if needed,
        // or just return success so the client can proceed.
        // For security, the backend should ideally be the one to update the 'emailVerified' flag.
        
        await docRef.delete();
        return { success: true };
    } else {
        // Increment attempts
        await docRef.update({ attempts: admin.firestore.FieldValue.increment(1) });
        throw new functions.https.HttpsError('permission-denied', 'Incorrect verification code.');
    }
});
