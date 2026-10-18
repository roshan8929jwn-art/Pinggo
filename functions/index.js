const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
const crypto = require("crypto");

admin.initializeApp();

/**
 * Creates nodemailer transport using environment variables or secrets
 */
function getTransporter() {
  const user = process.env.EMAIL_USER || process.env.GMAIL_USER || process.env.GMAIL_EMAIL || "roshan8929jwn@gmail.com";
  const pass = process.env.EMAIL_PASS || process.env.GMAIL_PASS;

  if (!pass) {
    console.error("CRITICAL: EMAIL_PASS or GMAIL_PASS secret not set in Firebase environment.");
    return null;
  }

  return {
    transporter: nodemailer.createTransport({
      service: "gmail",
      auth: { user, pass }
    }),
    senderEmail: user
  };
}

/**
 * sendOtp: Generates 6-digit OTP, stores hash in Firestore, sends email.
 * Single-use, 5-minute expiry, rate-limited with cooldown.
 */
exports.sendOtp = functions.runWith({ secrets: ["EMAIL_USER", "EMAIL_PASS", "GMAIL_PASS"] }).https.onCall(async (data, context) => {
  const email = (data.email || "").trim().toLowerCase();
  if (!email || !email.includes("@")) {
    throw new functions.https.HttpsError("invalid-argument", "A valid email address is required.");
  }

  const mailConfig = getTransporter();
  if (!mailConfig) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "Email provider is not configured. Please set EMAIL_PASS secret in Firebase Functions CLI: firebase functions:secrets:set EMAIL_PASS"
    );
  }

  const otpRef = admin.firestore().collection("otps").doc(email);
  const existingDoc = await otpRef.get();

  // Cooldown check (60 seconds)
  if (existingDoc.exists) {
    const data = existingDoc.data();
    const lastSentAt = data.lastSentAt || 0;
    const elapsed = Date.now() - lastSentAt;
    if (elapsed < 60000) {
      const waitSeconds = Math.ceil((60000 - elapsed) / 1000);
      throw new functions.https.HttpsError(
        "resource-exhausted",
        `Please wait ${waitSeconds}s before requesting a new code.`
      );
    }
  }

  // Generate 6-digit OTP
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
          <h1 style="color: #FF69B4; margin: 0; font-size: 28px; letter-spacing: -0.5px;">Pinggo 🐧</h1>
          <p style="color: #8E8E93; font-size: 13px; margin-top: 4px;">Instant Real-time Messaging</p>
        </div>
        <div style="background: #F8F8FA; padding: 24px; text-align: center; border-radius: 16px; border: 1px solid #F0E6ED; margin-bottom: 24px;">
          <p style="margin: 0 0 12px 0; font-size: 15px; color: #1C1C1E; font-weight: 500;">Your 6-digit verification code:</p>
          <div style="letter-spacing: 8px; font-size: 36px; font-weight: 800; color: #FF69B4; font-family: monospace;">${otp}</div>
          <p style="margin: 12px 0 0 0; font-size: 13px; color: #FF69B4; font-weight: 600;">Valid for 5 minutes only</p>
        </div>
        <p style="font-size: 13px; line-height: 1.6; color: #1C1C1E; margin-bottom: 20px;">
          Please enter this code in Pinggo to verify your email. Never share this code with anyone. Pinggo will never ask for your code.
        </p>
        <hr style="border: none; border-top: 1px solid #F0E6ED; margin-bottom: 16px;" />
        <p style="font-size: 11px; color: #8E8E93; text-align: center; margin: 0;">
          If you did not request this verification code, you can safely disregard this email.
        </p>
      </div>
    `,
  };

  try {
    await mailConfig.transporter.sendMail(mailOptions);
    return { success: true, message: "Verification OTP sent to your email." };
  } catch (error) {
    console.error("Error sending OTP email:", error);
    throw new functions.https.HttpsError("internal", `Email delivery failed: ${error.message || "Check SMTP credentials"}`);
  }
});

/**
 * verifyOtp: Validates single-use OTP on server, checks expiration (5 min) & max attempts (3).
 * Marks user as verified.
 */
exports.verifyOtp = functions.https.onCall(async (data, context) => {
  const email = (data.email || "").trim().toLowerCase();
  const otp = (data.otp || "").trim();

  if (!email || !otp) {
    throw new functions.https.HttpsError("invalid-argument", "Email and 6-digit OTP code are required.");
  }

  const otpRef = admin.firestore().collection("otps").doc(email);
  const doc = await otpRef.get();

  if (!doc.exists) {
    throw new functions.https.HttpsError("not-found", "No verification request found for this email. Please request a new code.");
  }

  const otpData = doc.data();

  // 1. Expiration check (5 minutes)
  if (Date.now() > otpData.expiresAt) {
    await otpRef.delete();
    throw new functions.https.HttpsError("deadline-exceeded", "Verification code has expired (5-minute limit). Please request a new code.");
  }

  // 2. Server-side attempt limit (3 attempts)
  if (otpData.attempts >= 3) {
    await otpRef.delete();
    throw new functions.https.HttpsError("permission-denied", "Too many failed attempts. Code has been invalidated. Please request a new one.");
  }

  // 3. Cryptographic hash comparison
  const hashedInput = crypto.createHash("sha256").update(otp).digest("hex");

  if (hashedInput === otpData.hashedOtp) {
    // Single-use: delete immediately upon success
    await otpRef.delete();

    // Mark user verified in Firestore
    const userQuery = await admin.firestore().collection("users")
      .where("email", "==", email)
      .limit(1)
      .get();

    if (!userQuery.empty) {
      const userDoc = userQuery.docs[0];
      await userDoc.ref.update({
        otpVerified: true,
        emailVerified: true
      });
    }

    // Also update if authenticated via Firebase Auth
    if (context.auth && context.auth.uid) {
      try {
        await admin.auth().updateUser(context.auth.uid, { emailVerified: true });
        await admin.firestore().collection("users").doc(context.auth.uid).update({
          otpVerified: true,
          emailVerified: true
        });
      } catch (authErr) {
        console.warn("Could not update auth user emailVerified:", authErr.message);
      }
    }

    return { success: true, message: "Email verified successfully." };
  } else {
    const nextAttempts = (otpData.attempts || 0) + 1;
    if (nextAttempts >= 3) {
      await otpRef.delete();
      throw new functions.https.HttpsError("permission-denied", "Incorrect code. Maximum attempts reached. Please request a new code.");
    } else {
      await otpRef.update({ attempts: nextAttempts });
      const remaining = 3 - nextAttempts;
      throw new functions.https.HttpsError("invalid-argument", `Invalid code. ${remaining} attempt${remaining > 1 ? "s" : ""} remaining.`);
    }
  }
});

/**
 * ping: Simple connectivity test for Cloud Functions.
 */
exports.ping = functions.https.onCall(async (data, context) => {
  return {
    success: true,
    message: "Pinggo Cloud Functions are online and reachable!",
    timestamp: Date.now(),
    projectId: process.env.GCLOUD_PROJECT || "gen-lang-client-0572544439"
  };
});

/**
 * awardMessageScore: Authoritative server-side Cloud Function to award chat points.
 * Validates authenticated sender, checks message eligibility and idempotency in score_events,
 * atomically updates chatScore, streak, longestStreak, and daily caps.
 */
exports.awardMessageScore = functions.https.onCall(async (data, context) => {
  if (!context.auth || !context.auth.uid) {
    throw new functions.https.HttpsError("unauthenticated", "User must be authenticated.");
  }
  const uid = context.auth.uid;
  const conversationId = (data.conversationId || "").trim();
  const messageId = (data.messageId || "").trim();

  if (!conversationId || !messageId) {
    throw new functions.https.HttpsError("invalid-argument", "conversationId and messageId are required.");
  }

  const db = admin.firestore();
  const msgDoc = await db.collection("conversations")
    .doc(conversationId)
    .collection("messages")
    .doc(messageId)
    .get();

  if (!msgDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Message does not exist.");
  }

  const msg = msgDoc.data();
  if (msg.senderId !== uid) {
    throw new functions.https.HttpsError("permission-denied", "Only the message sender can earn score.");
  }
  if (msg.isDeleted) {
    return { success: false, reason: "Deleted message is ineligible" };
  }

  const validTypes = ["text", "image", "voice", "file"];
  if (!validTypes.includes(msg.type)) {
    return { success: false, reason: `Message type ${msg.type} ineligible` };
  }
  if (msg.type === "text" && (!msg.text || !msg.text.trim())) {
    return { success: false, reason: "Empty text message ineligible" };
  }

  // Prevent score for self-messages in direct chats
  const convDoc = await db.collection("conversations").doc(conversationId).get();
  if (convDoc.exists && convDoc.data().type === "direct") {
    const participants = convDoc.data().participants || [];
    if (participants.filter(p => p !== uid).length === 0) {
      return { success: false, reason: "Self-conversations are ineligible for score" };
    }
  }

  const userRef = db.collection("users").doc(uid);
  const eventRef = userRef.collection("score_events").doc(messageId);

  // Authoritative UTC dates
  const msgTimestamp = msg.timestamp || Date.now();
  const msgDate = new Date(msgTimestamp);
  const todayDate = msgDate.toISOString().split("T")[0];
  const yesterdayDate = new Date(msgTimestamp - 86400000).toISOString().split("T")[0];

  return await db.runTransaction(async (transaction) => {
    // 1. Check idempotency: if event already processed, no-op
    const eventDoc = await transaction.get(eventRef);
    if (eventDoc.exists) {
      return { success: false, reason: "Already processed" };
    }

    const userDoc = await transaction.get(userRef);
    const userData = userDoc.exists ? userDoc.data() : {};

    const currentScore = Number(userData.chatScore || 0);
    const currentStreak = Number(userData.streakCount || 0);
    const longestStreak = Number(userData.longestStreak || 0);
    const lastActiveDate = userData.lastActiveDate || "";
    const dailyPointsDate = userData.dailyPointsDate || "";
    const dailyPointsCount = Number(userData.dailyPointsCount || 0);

    // Anti-spam daily cap (max 300 points per day)
    const todayPointsAccumulated = (dailyPointsDate === todayDate) ? dailyPointsCount : 0;
    const isDailyCapped = todayPointsAccumulated >= 300;
    const messagePoints = isDailyCapped ? 0 : 1;

    // Daily streak logic
    let newStreak = 1;
    let streakBonus = 0;
    let isStreakMilestone = false;

    if (lastActiveDate === todayDate) {
      newStreak = currentStreak <= 0 ? 1 : currentStreak;
      streakBonus = 0;
      isStreakMilestone = false;
    } else if (lastActiveDate === yesterdayDate) {
      newStreak = currentStreak + 1;
      streakBonus = 5;
      isStreakMilestone = true;
    } else {
      // Missed a day or first streak start
      newStreak = 1;
      streakBonus = lastActiveDate ? 0 : 5; // First time starting streak awards milestone bonus
      isStreakMilestone = !lastActiveDate;
    }

    const newLongestStreak = Math.max(longestStreak, newStreak);
    const pointsDelta = messagePoints + streakBonus;
    const newScore = currentScore + pointsDelta;

    // Idempotency event record
    transaction.set(eventRef, {
      messageId: messageId,
      conversationId: conversationId,
      messagePoints: messagePoints,
      streakBonus: streakBonus,
      pointsDelta: pointsDelta,
      date: todayDate,
      timestamp: Date.now()
    });

    // Update user document
    transaction.set(userRef, {
      chatScore: newScore,
      streakCount: newStreak,
      longestStreak: newLongestStreak,
      lastActiveDate: todayDate,
      dailyPointsDate: todayDate,
      dailyPointsCount: todayPointsAccumulated + pointsDelta
    }, { merge: true });

    return {
      success: true,
      pointsAwarded: pointsDelta,
      totalScore: newScore,
      streakBonus: streakBonus,
      currentStreak: newStreak,
      longestStreak: newLongestStreak,
      isStreakMilestone: isStreakMilestone
    };
  });
});
