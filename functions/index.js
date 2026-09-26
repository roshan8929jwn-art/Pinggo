const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
const crypto = require("crypto");

admin.initializeApp();

const gmailEmail = "roshan8929jwn@gmail.com";
// EMAIL_PASS is stored in Firebase Secrets

const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: gmailEmail,
    pass: process.env.EMAIL_PASS,
  },
});

/**
 * Generates a 6-digit OTP, hashes it, and stores it in Firestore.
 * Sends the plain OTP via email.
 */
exports.sendOtp = functions.https.onCall(async (data, context) => {
  const email = data.email;
  if (!email) {
    throw new functions.https.HttpsError("invalid-argument", "Email is required.");
  }

  // Rate limiting: Check last sent time
  const otpRef = admin.firestore().collection("otps").document(email);
  const doc = await otpRef.get();
  
  if (doc.exists) {
    const lastSent = doc.data().createdAt.toMillis();
    if (Date.now() - lastSent < 120000) { // 2 minute cooldown
      throw new functions.https.HttpsError("resource-exhausted", "Please wait 2 minutes before requesting a new OTP.");
    }
  }

  const otp = Math.floor(100000 + Math.random() * 900000).toString();
  const hashedOtp = crypto.createHash("sha256").update(otp).digest("hex");

  await otpRef.set({
    hashedOtp: hashedOtp,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    expiresAt: Date.now() + 600000, // 10 minutes
    attempts: 0
  });

  const mailOptions = {
    from: `"Pinggo Auth" <${gmailEmail}>`,
    to: email,
    subject: "Your Pinggo Verification Code",
    text: `Your verification code is: ${otp}. This code will expire in 10 minutes.`,
    html: `
      <div style="font-family: sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
        <h2 style="color: #FF1493;">Welcome to Pinggo!</h2>
        <p>Your verification code is:</p>
        <h1 style="font-size: 32px; letter-spacing: 5px; color: #333;">${otp}</h1>
        <p>This code will expire in 10 minutes. Do not share this code with anyone.</p>
        <hr style="border: none; border-top: 1px solid #eee;" />
        <p style="font-size: 12px; color: #999;">If you didn't request this, please ignore this email.</p>
      </div>
    `,
  };

  try {
    await transporter.sendMail(mailOptions);
    return { success: true, message: "OTP sent successfully." };
  } catch (error) {
    console.error("Error sending email:", error);
    throw new functions.https.HttpsError("internal", "Unable to send email.");
  }
});

/**
 * Verifies the OTP provided by the user.
 */
exports.verifyOtp = functions.https.onCall(async (data, context) => {
  const { email, otp } = data;
  if (!email || !otp) {
    throw new functions.https.HttpsError("invalid-argument", "Email and OTP are required.");
  }

  const otpRef = admin.firestore().collection("otps").document(email);
  const doc = await otpRef.get();

  if (!doc.exists) {
    throw new functions.https.HttpsError("not-found", "No OTP found for this email.");
  }

  const otpData = doc.data();
  
  if (Date.now() > otpData.expiresAt) {
    await otpRef.delete();
    throw new functions.https.HttpsError("deadline-exceeded", "OTP has expired.");
  }

  if (otpData.attempts >= 3) {
    await otpRef.delete();
    throw new functions.https.HttpsError("permission-denied", "Too many failed attempts. Please request a new OTP.");
  }

  const hashedInput = crypto.createHash("sha256").update(otp).digest("hex");

  if (hashedInput === otpData.hashedOtp) {
    // Success: Mark user as verified in Firestore
    const userQuery = await admin.firestore().collection("users")
      .where("email", "==", email)
      .limit(1)
      .get();

    if (!userQuery.empty) {
      const userDoc = userQuery.docs[0];
      await userDoc.ref.update({ otpVerified: true });
    }

    await otpRef.delete();
    return { success: true, message: "Email verified successfully." };
  } else {
    await otpRef.update({ attempts: otpData.attempts + 1 });
    throw new functions.https.HttpsError("invalid-argument", "Invalid OTP code.");
  }
});
