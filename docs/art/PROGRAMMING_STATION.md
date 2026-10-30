# Programming Station placeholder artwork

Four opaque 32x32 PNG block textures provide a dark gunmetal workstation with red
accents, a recessed code display, disk slot, ventilated casing and maintenance
plate. The front faces the placing player; existing unrotated stations use the
south face. This changes appearance only, not project storage or execution.

Source: [generated atlas](programming-station-atlas.png). Generated with the
built-in image-generation tool. The atlas was split into four faces and sampled
with nearest-neighbor pixel selection to package it at Minecraft texture size.

Runtime assets are in
`src/main/resources/assets/superfactorymanager/textures/blocks/programming_station_*.png`.

## Generation prompt

Create a production texture atlas for one Minecraft 1.7.10 mod block called the SFM Programming Station. This is actual flat block-face pixel artwork, NOT a rendered cube or concept sheet. Output a square image filled edge to edge by exactly four equally sized square texture tiles in a precise 2 by 2 grid, NO gaps, NO labels, NO margins. Top-left = FRONT: a dark recessed terminal screen in the upper half with a few simple red/pale amber code glyph pixels and a thick gray steel bezel; lower half has a slim red disk imprint slot and a tiny keypad/control strip. Top-right = TOP: charcoal industrial workstation panel, orderly inset plates, a central red SFM-like geometric circuit motif and subtle screws in corners. Bottom-left = SIDE/BACK: matching dark gunmetal casing with a recessed ventilation grille, a slim dark red accent stripe and steel corner brackets. Bottom-right = BOTTOM: matching sealed dark steel maintenance plate with four recessed corner screws. The 4 tiles share the exact same brushed gunmetal gray frame, nearly black recesses, desaturated red accents, a little warm gray edge light. Palette roughly #171a1f #25292f #383e46 #626a73 #8e434b #b35b5f; no blue or neon cyan. Premium polished industrial Minecraft pixel-art aesthetic, crisp big square pixels, carefully designed relief from one-pixel highlights and shadows at upper-left, strongly readable small icons, restrained contrast, no grime overload. Each tile must be drawn on an EXACT 32 by 32 logical pixel grid, enlarged with sharp nearest-neighbor square pixels. Align every logical pixel to that grid. No anti-aliasing, no gradients, no perspective, no photographic materials, no text, no logos, no drop shadows outside each tile. Complete opaque Minecraft block textures. Make the programming screen and disk slot distinguishable at tiny size.
