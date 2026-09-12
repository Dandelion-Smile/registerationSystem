export function formatReviewOpinion(comment) {
  const normalized = String(comment ?? "").trim();
  return normalized || "-";
}
