# AI use write-up (draft — edit so it is accurate and in your own words; max 500 words)

**Tool used:** Claude (Anthropic), https://claude.ai, used on 5 November 2026.

**How AI was used**
1. **Code review.** I pasted my Part 2 Kotlin code and asked Claude to review it. It identified problems, for
   example local posts staying "pending" after a server-assigned id, duplicate comments after sync, logout not
   clearing the outbox, and the Wi-Fi-only setting not being applied.
2. **Generating Part 3 additions.** Claude drafted code for language switching, Google sign-in, Firebase push,
   biometric unlock, data export, the Gradle build files, unit tests and the GitHub Actions workflow.
3. **Translations.** Claude produced first-draft isiZulu, Sesotho and Afrikaans strings. <describe who checked them>
4. **Documentation.** Claude drafted the README, release notes and Play Store checklist.

**What I did myself:** <list what you designed/wrote/tested — Part 1 design, backend, running on a physical phone,
fixing build errors, recording the demo video, etc.>

**Checking the output:** <describe how you verified it — compiled, ran on your phone, ran the unit tests, read and
understood each file, changed things.>

**Referencing (Harvard):** Anthropic (2026) *Claude* [Large language model]. Available at: https://claude.ai
[Accessed 5 November 2026]. Code adapted from official documentation is referenced in comments in the source files.
