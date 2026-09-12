export const STUDENT_PROFILE_INCOMPLETE_MESSAGE = "未完善个人信息无法正常报名赛事";
export const STUDENT_PROFILE_REMINDER_STORAGE_KEY = "student-profile-reminder-dismissed";

function pickFirstFilled(...values) {
  for (const value of values) {
    if (value !== undefined && value !== null && String(value).trim() !== "") {
      return value;
    }
  }
  return "";
}

export function getStudentRegistrationProfile(profile = {}, fallback = {}) {
  return {
    name: pickFirstFilled(
      profile.studentName,
      profile.name,
      profile.nickName,
      fallback.name,
    ),
    studentNo: pickFirstFilled(profile.studentNo, fallback.studentNo),
    college: pickFirstFilled(
      profile.dept?.deptName,
      profile.collegeName,
      profile.college,
      fallback.college,
    ),
    major: pickFirstFilled(profile.majorName, profile.major, fallback.major),
    className: pickFirstFilled(
      profile.className,
      profile.clazz,
      profile.class,
      fallback.className,
      fallback.clazz,
      fallback.class,
    ),
    phone: pickFirstFilled(profile.phonenumber, profile.phone, fallback.phone),
    email: pickFirstFilled(profile.email, fallback.email),
  };
}

export function isStudentRegistrationProfileComplete(profile = {}, fallback = {}) {
  return Object.values(getStudentRegistrationProfile(profile, fallback)).every(
    (value) => String(value || "").trim() !== "",
  );
}
