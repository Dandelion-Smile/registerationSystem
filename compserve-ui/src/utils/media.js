export function previewImageSrc(path) {
  if (!path) return "";
  if (typeof path !== "string") return "";
  const p = path.trim();
  if (p.startsWith("data:") || p.startsWith("blob:")) return p;
  const baseUrl = import.meta.env.VITE_APP_BASE_API;
  return `${baseUrl}/common/preview/url?url=${encodeURIComponent(p)}`;
}

export function proxyFileUrl(path, opts = {}) {
  if (!path) return "";
  if (typeof path !== "string") return "";
  const p = path.trim();
  const baseUrl = import.meta.env.VITE_APP_BASE_API;
  let url = `${baseUrl}/common/preview/url?url=${encodeURIComponent(p)}`;
  if (opts.name) url += `&name=${encodeURIComponent(opts.name)}`;
  if (opts.download) url += `&download=1`;
  return url;
}
