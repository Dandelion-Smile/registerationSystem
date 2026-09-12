const REGISTERED_COMPETITIONS_CACHE_KEY = "student-registered-competitions-cache";
const DEFAULT_REGISTERED_COMPETITIONS_CACHE_TTL = 15 * 1000;

function getRegisterTimestamp(item) {
  const rawValue =
    item?.registerTime ??
    item?.createdAt ??
    item?.createTime ??
    item?.submitTime ??
    "";
  const normalizedValue = String(rawValue).trim();

  if (!normalizedValue) {
    return Number.NEGATIVE_INFINITY;
  }

  const timestamp = Date.parse(normalizedValue.replace(/-/g, "/"));
  return Number.isNaN(timestamp) ? Number.NEGATIVE_INFINITY : timestamp;
}

function getStableRegisterId(item) {
  const rawValue =
    item?.registerId ??
    item?.id ??
    item?.displayRegisterId ??
    item?.competitionId ??
    0;
  const numericValue = Number(rawValue);
  return Number.isNaN(numericValue) ? 0 : numericValue;
}

export function sortRegisteredCompetitions(list = []) {
  return [...list].sort((left, right) => {
    const timeDiff = getRegisterTimestamp(right) - getRegisterTimestamp(left);
    if (timeDiff !== 0) {
      return timeDiff;
    }

    return getStableRegisterId(right) - getStableRegisterId(left);
  });
}

function getSessionStorage() {
  if (typeof window === "undefined" || !window.sessionStorage) {
    return null;
  }
  return window.sessionStorage;
}

export function readRegisteredCompetitionsCache() {
  const storage = getSessionStorage();
  if (!storage) {
    return null;
  }

  try {
    const raw = storage.getItem(REGISTERED_COMPETITIONS_CACHE_KEY);
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw);
    return {
      items: Array.isArray(parsed?.items) ? parsed.items : [],
      updatedAt: Number(parsed?.updatedAt) || 0,
    };
  } catch (error) {
    storage.removeItem(REGISTERED_COMPETITIONS_CACHE_KEY);
    return null;
  }
}

export function writeRegisteredCompetitionsCache(items = [], updatedAt = Date.now()) {
  const storage = getSessionStorage();
  if (!storage) {
    return;
  }

  storage.setItem(
    REGISTERED_COMPETITIONS_CACHE_KEY,
    JSON.stringify({
      items: Array.isArray(items) ? items : [],
      updatedAt,
    }),
  );
}

export function shouldReuseRegisteredCompetitionsCache(
  cache,
  ttlMs = DEFAULT_REGISTERED_COMPETITIONS_CACHE_TTL,
) {
  if (!cache || !cache.updatedAt) {
    return false;
  }
  return Date.now() - cache.updatedAt <= ttlMs;
}

export {
  DEFAULT_REGISTERED_COMPETITIONS_CACHE_TTL,
  REGISTERED_COMPETITIONS_CACHE_KEY,
  getRegisterTimestamp,
};
