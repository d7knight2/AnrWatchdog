# Memory Leak Examples (Demo App)

This document expands the memory leak scenarios shown in the demo UI.

## 1) Static Activity Reference

**Leak pattern**
- A static/singleton field stores an `Activity` instance.
- After rotation or navigation, the old activity cannot be collected.

**Fix**
- Avoid holding `Activity` in static fields.
- If global access is required, store `applicationContext`.

## 2) Unregistered Listener

**Leak pattern**
- A callback/listener is registered and never unregistered.
- The listener captures a Fragment/View and keeps it alive.

**Fix**
- Register in lifecycle-aware methods (`onStart`/`onResume`).
- Unregister in matching teardown (`onStop`/`onPause`).

## 3) Long-running Coroutine Capturing View

**Leak pattern**
- A coroutine launched in a broad scope captures a `View` or binding.
- The job outlives the view lifecycle.

**Fix**
- Use `viewLifecycleOwner.lifecycleScope` in fragments.
- Cancel custom jobs in `onDestroyView` or `onDestroy`.

## 4) Oversized Bitmap Cache

**Leak pattern**
- Bitmaps are added to a cache without a size cap.
- Memory usage grows indefinitely and triggers frequent GC.

**Fix**
- Use `LruCache` with a strict max size.
- Evict aggressively when app is backgrounded.

## 5) Fragment ViewBinding Not Cleared

**Leak pattern**
- Fragment keeps a non-null `binding` after `onDestroyView`.
- Entire view tree is retained.

**Fix**
- Set `binding = null` in `onDestroyView`.

## Verification tips

- Use LeakCanary retained object reports after repeating navigation.
- Simulate real user interactions (rotate, switch tabs, background app).
- Confirm memory settles after interaction bursts.
