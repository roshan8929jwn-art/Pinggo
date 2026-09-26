/**
 * Firebase Cloud Functions implementation for Pinggo Email OTP Verification.
 * 
 * REQUIREMENTS:
 * 1. Firebase Project must be on the BLAZE (Pay-as-you-go) plan to allow outgoing network requests to email providers.
 * 2. You need an email account (e.g., Gmail with App Password, or SendGrid/Mailgun API).
 * 
 * DEPLOYMENT (Using Firebase CLI):
 * 1. firebase init functions (select JavaScript)
 * 2. Set secrets:
 *    firebase functions:secrets:set EMAIL_USER
 *    firebase functions:secrets:set EMAIL_PASS
 * 3. Copy this code to functions/index.js
 * 4. Run: firebase deploy --only functions
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');
const crypto = require('crypto');

admin.initializeApp();

/**
 * sendOtp: Generates and sends a 6-digit OTP.
 */
exports.sendOtp = functions.runWith({ secrets: ["EMAIL_USER", "EMAIL_PASS"] }).https.onCall(async (data, context) => {
    const email = data.email;
    const uid = context.auth ? context.auth.uid : null;

    if (!email || !email.includes('@')) {
        throw new functions.https.HttpsError('invalid-argument', 'A valid email address is required.');
    }

    const db = admin.firestore();
    const docRef = db.collection('otp_challenges').doc(email);
    const snapshot = await docRef.get();

    // 1. Abuse Protection: Cooldown check (2 minutes)
    if (snapshot.exists) {
        const lastSentAt = snapshot.data().lastSentAt;
        if (lastSentAt && (Date.now() - lastSentAt < 120000)) {
            throw new functions.https.HttpsError('resource-exhausted', 'Please wait 2 minutes before requesting another code.');
        }
    }

    // 2. Generate 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const hashedOtp = crypto.createHash('sha256').update(otp).digest('hex');
    const expiresAt = Date.now() + 600000; // 10 minutes expiry

    // 3. Store challenge securely
    await docRef.set({
        hashedOtp: hashedOtp,
        expiresAt: expiresAt,
        lastSentAt: Date.now(),
        attempts: 0,
        uid: uid // Optional: link to a specific user if authenticated
    });

    // 4. Configure Mailer
    // Note: If using Gmail, you MUST use an "App Password" (not your main password).
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
        subject: 'Pinggo Verification Code',
        html: `
            <div style="font-family: 'Helvetica Neue', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #E5E5EA; border-radius: 16px;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #FF1493; margin: 0;">Pinggo 🐧</h1>
                    <p style="color: #8E8E93; font-size: 14px; margin-top: 5px;">Same Vibes, New Experience</p>
                </div>
                <div style="background: #F2F2F7; padding: 30px; text-align: center; border-radius: 12px; margin-bottom: 30px;">
                    <p style="margin: 0 0 15px 0; font-size: 16px; color: #1C1C1E;">Your verification code is:</p>
                    <h2 style="letter-spacing: 8px; font-size: 42px; margin: 0; color: #FF1493;">${otp}</h2>
                    <p style="margin: 15px 0 0 0; font-size: 14px; color: #8E8E93;">Expires in 10 minutes</p>
                </div>
                <p style="font-size: 14px; line-height: 1.5; color: #3A3A3C;">
                    Enter this code in the app to verify your identity. If you didn't request this, you can ignore this email.
                </p>
                <div style="border-top: 1px solid #E5E5EA; margin-top: 30px; padding-top: 20px; text-align: center; color: #8E8E93; font-size: 12px;">
                    &copy; ${new Date().getFullYear()} Pinggo Messenger. Secure OTP Verification.
                </div>
            </div>
        `
    };

    try {
        await transporter.sendMail(mailOptions);
        return { success: true };
    } catch (error) {
        console.error('Mail transport error:', error);
        throw new functions.https.HttpsError('internal', 'Unable to send email. Check backend configuration.');
    }
});

/**
 * verifyOtp: Validates OTP and marks user as verified in Firestore.
 */
exports.verifyOtp = functions.https.onCall(async (data, context) => {
    const email = data.email;
    const otp = data.otp;

    if (!email || !otp) {
        throw new functions.https.HttpsError('invalid-argument', 'Email and code are required.');
    }

    const db = admin.firestore();
    const docRef = db.collection('otp_challenges').doc(email);
    const doc = await docRef.get();

    if (!doc.exists) {
        throw new functions.https.HttpsError('not-found', 'No active verification request found.');
    }

    const challenge = doc.data();

    // 1. Check expiration
    if (Date.now() > challenge.expiresAt) {
        await docRef.delete();
        throw new functions.https.HttpsError('deadline-exceeded', 'The verification code has expired.');
    }

    // 2. Check attempt limits (Abuse Protection)
    if (challenge.attempts >= 3) {
        throw new functions.https.HttpsError('permission-denied', 'Too many failed attempts. Please request a new code.');
    }

    // 3. Verify Hash
    const inputHash = crypto.createHash('sha256').update(otp).digest('hex');
    if (inputHash === challenge.hashedOtp) {
        // Success: Clean up challenge
        await docRef.delete();

        // 4. Update User Profile if authenticated
        // If the user is already logged in, we mark their profile as verified.
        // If not, they'll be marked after login (client-side will proceed to profile setup).
        const targetUid = context.auth ? context.auth.uid : challenge.uid;
        if (targetUid) {
            await db.collection('users').doc(targetUid).update({
                otpVerified: true,
                emailVerified: true // Also mark standard Firebase email as verified for convenience
            });
        }

        return { success: true };
    } else {
        // Increment attempts
        await docRef.update({ attempts: admin.firestore.FieldValue.increment(1) });
        throw new functions.https.HttpsError('permission-denied', 'Invalid verification code.');
    }
});
