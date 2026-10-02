# Grok 95% Visual Audit — v6.60

## Target
The supplied Grok reference establishes the visual target for the primary dashboard:
- near-black/navy background
- warm gold primary accent
- thin gold/bronze card borders
- large rounded hero/streak card
- four compact statistics cards in one row
- three full-width ready-review cards
- full-width gold Add Word CTA
- five-item bottom navigation with a wide selected gold pill
- outlined inactive icons and filled active navigation icon
- RTL Persian hierarchy with controlled vertical rhythm

## v6.60 implementation
- Home geometry is theme-owned through homeHeroHeight, homeReviewHeight, homeCtaHeight and homeBottomGap.
- Grok statistics remain GRID_4_COLUMNS.
- Grok navigation uses a full-width selected pill instead of the narrower Material default indicator.
- Active/inactive icon style and icon scaling remain theme-owned.
- Custom theme JSON now persists the complete ThemeDesign contract, including metrics and visual strategy values.

## Acceptance gate
Code and CI acceptance for this checkpoint are complete; visual acceptance remains screenshot-based. Final acceptance requires:
1. Build + Unit Test green.
2. Instrumentation + upgrade gate green.
3. Runtime screenshot of the Grok Home screen at the reference device dimensions.
4. Visual comparison against the supplied reference.
5. If the visual comparison remains below the requested 95% similarity, another refinement pass is required.

## Non-negotiable rule
CI is green, but CI alone is not proof of pixel-level visual similarity.
