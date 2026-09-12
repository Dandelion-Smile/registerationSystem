import assert from "node:assert/strict";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(currentDir, "..");

const moduleUrl = pathToFileURL(
  path.join(projectRoot, "src/utils/reviewOpinion.js")
).href;

const { formatReviewOpinion } = await import(moduleUrl);

assert.equal(typeof formatReviewOpinion, "function");
assert.equal(formatReviewOpinion(undefined), "-");
assert.equal(formatReviewOpinion(null), "-");
assert.equal(formatReviewOpinion("   "), "-");
assert.equal(formatReviewOpinion("评审意见很好"), "评审意见很好");
