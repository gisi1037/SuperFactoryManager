# Programming Station artwork

Four opaque **32x32** PNG faces use a shared 14-color steel/red palette. The
front has three high-contrast code rows, a large red disk drive and an amber
status lamp. The side has broad vents; the top has a bold circuit symbol.
The bottom is a simple maintenance plate. Orientation and behavior are unchanged.

Runtime assets:
`src/main/resources/assets/superfactorymanager/textures/blocks/programming_station_*.png`.

[Final 64x64 face atlas](programming-station-atlas.png) contains four actual
32x32 faces: front, top, side and bottom in reading order.
[Enlarged pixel preview](programming-station-preview.png) uses nearest-neighbor
scaling of those exact final PNGs. Judge legibility from these packaged faces,
not from high-resolution generation output.

## Production

Generated with the built-in image-generation tool, one image per face. Final
faces are sampled at pixel centers onto a 32x32 grid and quantized to the same
14 colors. No filtered reduction, gradients or interpolated edge colors are
used. Original generation output remains private; the public atlas and runtime
assets contain the final pixel artwork only.

Palette: `#10141a #1d232b #2b313a #3c434c #4b535e #68727d #929da8
#c3c8c8 #7b272f #b74448 #d75956 #ef7868 #e5e3cc #e6b760`.

## Generation prompts

### front

Use case: stylized-concept. Asset type: production Minecraft 1.7.10 block-face texture. Design completely NEW artwork at EXACTLY 32 by 32 LOGICAL PIXELS enlarged to fill a square canvas. One flat orthographic face fills the ENTIRE image edge-to-edge. This is a pixel texture, not a rendered block, not a product illustration. Chunky hard-edged true pixel art drawn on a uniform 32x32 grid. Each logical pixel is a single solid color square. Use 10 to 14 solid colors only, NO gradients, noise, glow, antialiasing, microtexture or thin subpixel marks. Broad, deliberately legible shapes that survive reduction to 32x32. Dark industrial SFM computer workstation: medium gunmetal casing #4b515b, black recess #10141a, steel edge highlights #929ba5, deep edge shadows #252a33, saturated brick red #b74448 with coral red highlight #e06b62, ivory screen glyphs #e3e3cc and warm amber #e6b760. 2-logical-pixel steel outer border with simple light upper-left bevel and dark bottom-right edge. Keep generous contrast; no muddy gray-on-gray. No lettering, watermark, perspective, drop shadow, margins, multiple tiles or scene. FRONT FACE: a clearly recognizable programming terminal. Huge rectangular black computer monitor covers most of upper two-thirds: screen approximately logical x5..26,y5..19 with a thick raised metal bezel. On screen place THREE distinct big terminal rows, each 2 logical pixels tall: coral red > prompt with 8-pixel ivory horizontal bar, second shorter ivory bar, and third amber bar. Large dark spaces between rows; tiny marks forbidden. Lower third: very obvious bright red disk-drive bezel spanning x5..24,y24..28, with a BLACK HORIZONTAL DISK SLOT 14x2 logical pixels centered inside. To right of disk slot one square amber status lamp. Bottom lip steel. Just two minimal corner screws. Readable as screen plus disk drive at actual 32x32 resolution.

### top

Use case: stylized-concept. Asset type: production Minecraft 1.7.10 block-face texture. Design completely NEW artwork at EXACTLY 32 by 32 LOGICAL PIXELS enlarged to fill a square canvas. One flat orthographic face fills the ENTIRE image edge-to-edge. This is a pixel texture, not a rendered block, not a product illustration. Chunky hard-edged true pixel art drawn on a uniform 32x32 grid. Each logical pixel is a single solid color square. Use 10 to 14 solid colors only, NO gradients, noise, glow, antialiasing, microtexture or thin subpixel marks. Broad, deliberately legible shapes that survive reduction to 32x32. Dark industrial SFM computer workstation: medium gunmetal casing #4b515b, black recess #10141a, steel edge highlights #929ba5, deep edge shadows #252a33, saturated brick red #b74448 with coral red highlight #e06b62, ivory screen glyphs #e3e3cc and warm amber #e6b760. 2-logical-pixel steel outer border with simple light upper-left bevel and dark bottom-right edge. Keep generous contrast; no muddy gray-on-gray. No lettering, watermark, perspective, drop shadow, margins, multiple tiles or scene. TOP FACE: simple matching industrial workstation lid. A broad light-gray raised rectangular center plate with a near-black inset and a BOLD RED CIRCUIT SYMBOL: central red 6x6 square connected by thick 2-pixel traces to three small square terminals. Symbol large and legible in center of panel, around logical x8..24,y8..24. Two modest corner fasteners only. Keep the casing mid-gray so the red and the recess stand out. No screen or disk slot on top.

### side

Use case: stylized-concept. Asset type: production Minecraft 1.7.10 block-face texture. Design completely NEW artwork at EXACTLY 32 by 32 LOGICAL PIXELS enlarged to fill a square canvas. One flat orthographic face fills the ENTIRE image edge-to-edge. This is a pixel texture, not a rendered block, not a product illustration. Chunky hard-edged true pixel art drawn on a uniform 32x32 grid. Each logical pixel is a single solid color square. Use 10 to 14 solid colors only, NO gradients, noise, glow, antialiasing, microtexture or thin subpixel marks. Broad, deliberately legible shapes that survive reduction to 32x32. Dark industrial SFM computer workstation: medium gunmetal casing #4b515b, black recess #10141a, steel edge highlights #929ba5, deep edge shadows #252a33, saturated brick red #b74448 with coral red highlight #e06b62, ivory screen glyphs #e3e3cc and warm amber #e6b760. 2-logical-pixel steel outer border with simple light upper-left bevel and dark bottom-right edge. Keep generous contrast; no muddy gray-on-gray. No lettering, watermark, perspective, drop shadow, margins, multiple tiles or scene. SIDE/BACK FACE: simple matching industrial computer casing. Three very broad BLACK horizontal ventilation slots, each about 20 logical pixels wide and 3 logical pixels tall, separated by thick raised gray steel bars, filling center x6..25,y7..22. One unmistakable brick-red horizontal band across lower third at y25..27. Two small steel fasteners at opposite corners. Strong shadows and highlights on the vent frame. Legible black vents and red stripe at 32x32.

### bottom

Use case: stylized-concept. Asset type: production Minecraft 1.7.10 block-face texture. Design completely NEW artwork at EXACTLY 32 by 32 LOGICAL PIXELS enlarged to fill a square canvas. One flat orthographic face fills the ENTIRE image edge-to-edge. This is a pixel texture, not a rendered block, not a product illustration. Chunky hard-edged true pixel art drawn on a uniform 32x32 grid. Each logical pixel is a single solid color square. Use 10 to 14 solid colors only, NO gradients, noise, glow, antialiasing, microtexture or thin subpixel marks. Broad, deliberately legible shapes that survive reduction to 32x32. Dark industrial SFM computer workstation: medium gunmetal casing #4b515b, black recess #10141a, steel edge highlights #929ba5, deep edge shadows #252a33, saturated brick red #b74448 with coral red highlight #e06b62, ivory screen glyphs #e3e3cc and warm amber #e6b760. 2-logical-pixel steel outer border with simple light upper-left bevel and dark bottom-right edge. Keep generous contrast; no muddy gray-on-gray. No lettering, watermark, perspective, drop shadow, margins, multiple tiles or scene. BOTTOM FACE: simple matching sealed gunmetal maintenance cover. Broad light steel outer rim, single large charcoal rectangular inset maintenance plate, only FOUR distinct square steel screw heads near its corners. A small red 6x2 rectangular service marker near the bottom center. No vents, screen, ornate details or decorative symbols. Bold clean 32x32 pixel-art construction.
