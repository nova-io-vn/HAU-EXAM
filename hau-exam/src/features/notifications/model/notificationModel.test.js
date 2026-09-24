import assert from "node:assert/strict";
import test from "node:test";
import { normalizeNotification } from "./notificationModel.js";

test("normalizes the backend read field", () => {
  assert.equal(normalizeNotification({ id: "n1", read: true }).isRead, true);
  assert.equal(normalizeNotification({ id: "n2", read: false }).isRead, false);
});

test("dedicated isRead compatibility does not override backend read", () => {
  assert.equal(
    normalizeNotification({ id: "n1", read: false, isRead: true }).isRead,
    false,
  );
});
