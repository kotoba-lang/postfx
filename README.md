# kotoba-lang/postfx

Zero-dep portable `.cljc` — restored from the legacy `kami-engine/kami-postfx` Rust crate
(`kami-postfx/src/lib.rs`, 332 lines, `kotoba-lang/kami-engine` @
`a8368f9c0d784dbc9d11e8fa8f407aa95c7ce4fa`, deleted in PR #82 "Remove Rust workspace from
kami-engine") as part of the **clj-wgsl migration** (ADR-2607010930, `com-junkawasaki/root`).

## Status

Restored. `src/postfx.cljc` ports the whole original crate: the `PostEffect` pass
configuration variants (bloom, outline/cel-shading, vignette, CRT scanlines, color
grading, pixelate, SSAO, depth of field, screen-space reflections, ACES filmic
tonemapping, film grain, chromatic aberration, god rays), the `PostFxPipeline`
struct/constructors, the four curated presets (`nintendo`, `retro`, `final-fantasy`,
`baminiku-character`), and the `#[repr(C)] Pod/Zeroable` GPU-uniform param layout structs
(`bloom-params`, `outline-params`, `ssao-params`, `depth-of-field-params`, `ssr-params`,
`aces-tonemap-params`, `god-rays-params`), all as plain CLJC data + pure constructor
functions.

Ledger class `:port-to-WGSL-compute`: the original `lib.rs` contains no actual WGSL/wgpu
render-pass *execution* code to exclude — it is entirely pure pass-configuration data and
GPU-uniform layout description. Per owner decision this is restored as plain interim CLJC
(not authored WGSL compute/fragment shaders); no wgpu code was excluded because none was
present to exclude, beyond the crate's own top-level note that native execution (wgpu)
stays substrate.

All 3 original Rust `#[test]`s ported 1:1 to `test/postfx_test.cljc` (+1 namespace-load
smoke test) — 4 tests / 7 assertions, 0 failures. Pure data + pure functions throughout;
no IO/GPU.

## Develop

```bash
clojure -M:test
```
