/**
 * Firebase Cloud Functions implementation for Pinggo Email OTP Verification.
 * 
 * REQUIREMENTS:
 * 1. Firebase Project must be on the Blaze plan (pay-as-you-go) for external network requests (Nodemailer/SMTP).
 * 2. Set Firebase Functions secrets:
 *    firebase functions:secrets:set EMAIL_USER
 *    firebase functions:secrets:set EMAIL_PASS
 * 
 * 3. Deploy:
 *    firebase deploy --only functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
const crypto = require("crypto");

admin.initializeApp();

function getTransporter() {
  const user = process.env.EMAIL_USER || process.env.GMAIL_EMAIL || "roshan8929jwn@gmail.com";
  const pass = process.env.EMAIL_PASS || process.env.GMAIL_PASS;

  if (!pass) return null;

  return {
    transporter: nodemailer.createTransport({
      service: "gmail",
      auth: { user, pass }
    }),
    senderEmail: user
  };
}

exports.sendOtp = functions.runWith({ secrets: ["EMAIL_USER", "EMAIL_PASS", "GMAIL_PASS"] }).https.onCall(async (data, context) => {
  const email = (data.email || "").trim().toLowerCase();
  if (!email || !email.includes("@")) {
    throw new functions.https.HttpsError("invalid-argument", "A valid email address is required.");
  }

  const mailConfig = getTransporter();
  if (!mailConfig) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "Email provider is not configured. Please set EMAIL_PASS secret via Firebase CLI: firebase functions:secrets:set EMAIL_PASS"
    );
  }

  const otpRef = admin.firestore().collection("otps").doc(email);
  const existingDoc = await otpRef.get();

  if (existingDoc.exists) {
    const elapsed = Date.now() - (existingDoc.data().lastSentAt || 0);
    if (elapsed < 60000) {
      const waitSeconds = Math.ceil((60000 - elapsed) / 1000);
      throw new functions.https.HttpsError("resource-exhausted", `Please wait ${waitSeconds}s before requesting a new code.`);
    }
  }

  const otp = Math.floor(100000 + Math.random() * 900000).toString();
  const hashedOtp = crypto.createHash("sha256").update(otp).digest("hex");
  const expiresAt = Date.now() + 5 * 60 * 1000; // 5 minutes

  await otpRef.set({
    hashedOtp: hashedOtp,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    lastSentAt: Date.now(),
    expiresAt: expiresAt,
    attempts: 0,
    email: email
  });

  const mailOptions = {
    from: `"Pinggo Messenger" <${mailConfig.senderEmail}>`,
    to: email,
    subject: "Your Pinggo Verification Code",
    text: `Your verification code is: ${otp}. This code will expire in 5 minutes.`,
    html: `
      <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 28px; border: 1px solid #FFB6D9; border-radius: 20px; background-color: #FFFFFF;">
        <div style="text-align: center; margin-bottom: 24px;">
          <h1 style="color: #FF69B4; margin: 0; font-size: 28px;">Pinggo 🐧</h1>
          <p style="color: #8E8E93; font-size: 13px; margin-top: 4px;">Instant Real-time Messaging</p>
        </div>
        <div style="background: #F8F8FA; padding: 24px; text-align: center; border-radius: 16px; border: 1px solid #F0E6ED; margin-bottom: 24px;">
          <p style="margin: 0 0 12px 0; font-size: 15px; color: #1C1C1E; font-weight: 500;">Your 6-digit verification code:</p>
          <div style="letter-spacing: 8px; font-size: 36px; font-weight: 800; color: #FF69B4; font-family: monospace;">${otp}</div>
          <p style="margin: 12px 0 0 0; font-size: 13px; color: #FF69B4; font-weight: 600;">Valid for 5 minutes only</p>
        </div>
        <p style="font-size: 13px; line-height: 1.6; color: #1C1C1E;">
          Enter this code in Pinggo to verify your email. Never share this code with anyone.
        </p>
      </div>
    `,
  };

  try {
    await mailConfig.transporter.sendMail(mailOptions);
    return { success: true, message: "Verification OTP sent." };
  } catch (error) {
    console.error("Mail send error:", error);
    throw new functions.https.HttpsError("internal", `Email delivery failed: ${error.message || "Check SMTP credentials"}`);
  }
});

exports.verifyOtp = functions.https.onCall(async (data, context) => {
  const email = (data.email || "").trim().toLowerCase();
  const otp = (data.otp || "").trim();

  if (!email || !otp) {
    throw new functions.https.HttpsError("invalid-argument", "Email and 6-digit OTP code are required.");
  }

  const otpRef = admin.firestore().collection("otps").doc(email);
  const doc = await otpRef.get();

  if (!doc.exists) {
    throw new functions.https.HttpsError("not-found", "No verification request found for this email.");
  }

  const otpData = doc.data();

  if (Date.now() > otpData.expiresAt) {
    await otpRef.delete();
    throw new functions.https.HttpsError("deadline-exceeded", "Verification code has expired (5-minute limit).");
  }

  if (otpData.attempts >= 3) {
    await otpRef.delete();
    throw new functions.https.HttpsError("permission-denied", "Too many failed attempts. Code has been invalidated.");
  }

  const hashedInput = crypto.createHash("sha256").update(otp).digest("hex");

  if (hashedInput === otpData.hashedOtp) {
    await otpRef.delete();

    const userQuery = await admin.firestore().collection("users")
      .where("email", "==", email)
      .limit(1)
      .get();

    if (!userQuery.empty) {
      await userQuery.docs[0].ref.update({
        otpVerified: true,
        emailVerified: true
      });
    }

    if (context.auth && context.auth.uid) {
      try {
        await admin.auth().updateUser(context.auth.uid, { emailVerified: true });
        await admin.firestore().collection("users").doc(context.auth.uid).update({
          otpVerified: true,
          emailVerified: true
        });
      } catch (e) {
        console.warn("Auth update note:", e.message);
      }
    }

    return { success: true, message: "Email verified successfully." };
  } else {
    const nextAttempts = (otpData.attempts || 0) + 1;
    if (nextAttempts >= 3) {
      await otpRef.delete();
      throw new functions.https.HttpsError("permission-denied", "Incorrect code. Maximum attempts reached.");
    } else {
      await otpRef.update({ attempts: nextAttempts });
      const remaining = 3 - nextAttempts;
      throw new functions.https.HttpsError("invalid-argument", `Invalid code. ${remaining} attempt${remaining > 1 ? "s" : ""} remaining.`);
    }
  }
});
