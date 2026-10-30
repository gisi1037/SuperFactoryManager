# Programming Station placeholder artwork

Four opaque 16x16 PNG block textures provide a dark gunmetal workstation with red
accents, a recessed code display, disk slot, ventilated casing and maintenance
plate. The front faces the placing player; existing unrotated stations use the
south face. This changes appearance only, not project storage or execution.

Source: [generated atlas](programming-station-atlas.png). Generated with the
built-in image-generation tool. The atlas was split into four faces and reduced
with filtered sampling to retain small details at 16x16. The enlarged preview
uses nearest-neighbor scaling; runtime faces are genuine 16x16 PNGs.

Runtime assets are in
`src/main/resources/assets/superfactorymanager/textures/blocks/programming_station_*.png`.

## Generation prompt

Use case: precise-object-edit. Edit target: the supplied SFM Programming Station 2x2 block texture atlas. Simplify the SAME four faces into EXACTLY 16 by 16 logical pixels EACH, enlarged as sharp square pixel art. Entire image must be 32 by 32 logical pixels enlarged, split into equal 2x2 tiles edge to edge without gaps, margins, labels or perspective. Top-left FRONT: charcoal steel frame, upper recessed black screen with just a few big red and pale gray code marks, bottom a red disk slot and small controls. Top-right TOP: matching steel panel with a simple red circuit cross in middle. Bottom-left SIDE: gray steel vents, red horizontal stripe below vents. Bottom-right BOTTOM: simple sealed steel plate and four screws. Preserve gunmetal gray/desaturated red palette and industrial inset panel style of input. Eliminate fine details that cannot survive 16x16. Every detail uses full integer pixels on the 16x16 grid; no antialiasing, gradients or noise. No words, logos, transparency, photorealism or rendered cube. Production Minecraft 1.7.10 textures.
