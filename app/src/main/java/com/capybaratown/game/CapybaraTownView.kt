package com.capybaratown.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

class CapybaraTownView(context: Context) : View(context) {
    private val game = GameModel()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(65, 52, 37)
        textSize = 34f
    }
    private val uiPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val helpButton = RectF()
    private val pauseButton = RectF()
    private val resetButton = RectF()
    private var lastFrameNanos = 0L
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var partyPulse = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.nanoTime()
        val dt = if (lastFrameNanos == 0L) 0f else (now - lastFrameNanos) / 1_000_000_000f
        lastFrameNanos = now
        game.tick(dt)
        partyPulse += dt

        calculateWorldTransform()
        canvas.drawColor(Color.rgb(244, 225, 190))
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)
        drawTown(canvas)
        drawSnacks(canvas)
        drawNpcs(canvas)
        drawPlayer(canvas)
        if (game.hasWon()) {
            drawTownParty(canvas)
        }
        canvas.restore()
        drawUi(canvas)
        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) {
            return true
        }
        val x = event.x
        val y = event.y
        when {
            helpButton.contains(x, y) -> game.tryHelpNearby()
            pauseButton.contains(x, y) -> game.togglePaused()
            resetButton.contains(x, y) -> game.reset()
            else -> {
                val worldX = (x - offsetX) / scale
                val worldY = (y - offsetY) / scale
                game.setMoveTarget(worldX, worldY)
            }
        }
        invalidate()
        return true
    }

    private fun calculateWorldTransform() {
        val availableHeight = height - 330f
        scale = min(width / GameModel.WORLD_WIDTH, availableHeight / GameModel.WORLD_HEIGHT)
        offsetX = (width - GameModel.WORLD_WIDTH * scale) / 2f
        offsetY = 190f
    }

    private fun drawTown(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(151, 196, 120)
        canvas.drawRect(0f, 0f, GameModel.WORLD_WIDTH, GameModel.WORLD_HEIGHT, paint)
        drawGrassTexture(canvas)

        paint.color = Color.rgb(180, 148, 94)
        paint.strokeWidth = 64f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val path = Path().apply {
            moveTo(440f, 120f)
            cubicTo(320f, 315f, 725f, 400f, 600f, 620f)
            cubicTo(470f, 830f, 225f, 735f, 210f, 1080f)
            cubicTo(360f, 1180f, 610f, 1145f, 760f, 1210f)
        }
        canvas.drawPath(path, paint)
        canvas.drawLine(170f, 620f, 760f, 760f, paint)
        paint.color = Color.rgb(229, 197, 136)
        paint.strokeWidth = 48f
        canvas.drawPath(path, paint)
        canvas.drawLine(170f, 620f, 760f, 760f, paint)
        paint.color = Color.argb(68, 255, 247, 210)
        paint.strokeWidth = 9f
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.FILL
        drawHouse(canvas, 130f, 210f, Color.rgb(179, 126, 81), Color.rgb(99, 75, 50), "care")
        drawSnackStall(canvas, 610f, 400f)
        drawHouse(canvas, 455f, 910f, Color.rgb(156, 131, 196), Color.rgb(85, 72, 119), "quiet")

        drawPond(canvas)
        drawGardenRow(canvas, 620f, 625f)
        drawGardenRow(canvas, 662f, 646f)
        drawBurrow(canvas, 408f, 1030f)

        drawTree(canvas, 94f, 138f, 1.05f)
        drawTree(canvas, 790f, 170f, 0.92f)
        drawTree(canvas, 108f, 1120f, 0.88f)
        drawBush(canvas, 760f, 560f)
        drawBush(canvas, 116f, 730f)
        drawFlowerPatch(canvas, 115f, 566f)
        drawFlowerPatch(canvas, 710f, 1040f)
    }

    private fun drawGrassTexture(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        for (i in 0 until 56) {
            val x = ((i * 137) % 860 + 20).toFloat()
            val y = ((i * 211) % 1230 + 35).toFloat()
            paint.color = if (i % 2 == 0) Color.argb(70, 99, 151, 82) else Color.argb(54, 226, 235, 164)
            canvas.drawOval(RectF(x, y, x + 10f + i % 8, y + 4f), paint)
        }
    }

    private fun drawHouse(canvas: Canvas, x: Float, y: Float, wall: Int, roof: Int, sign: String) {
        drawShadow(canvas, x + 82f, y + 165f, 98f, 24f)
        paint.style = Paint.Style.FILL
        paint.color = wall
        canvas.drawRoundRect(RectF(x, y + 55f, x + 165f, y + 165f), 18f, 18f, paint)
        paint.color = Color.argb(55, 255, 255, 235)
        canvas.drawRoundRect(RectF(x + 16f, y + 70f, x + 150f, y + 105f), 14f, 14f, paint)
        paint.color = roof
        val roofPath = Path().apply {
            moveTo(x - 20f, y + 70f)
            lineTo(x + 82f, y)
            lineTo(x + 185f, y + 70f)
            close()
        }
        canvas.drawPath(roofPath, paint)
        paint.color = Color.rgb(78, 58, 43)
        canvas.drawRoundRect(RectF(x + 66f, y + 105f, x + 104f, y + 165f), 10f, 10f, paint)
        paint.color = Color.rgb(255, 238, 178)
        canvas.drawRoundRect(RectF(x + 28f, y + 88f, x + 56f, y + 118f), 8f, 8f, paint)
        canvas.drawRoundRect(RectF(x + 118f, y + 88f, x + 146f, y + 118f), 8f, 8f, paint)
        textPaint.color = Color.rgb(76, 58, 43)
        textPaint.textSize = 22f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(sign, x + 82f, y + 146f, textPaint)
    }

    private fun drawPond(canvas: Canvas) {
        drawShadow(canvas, 248f, 462f, 158f, 25f)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(93, 156, 185)
        canvas.drawOval(RectF(95f, 355f, 400f, 510f), paint)
        paint.color = Color.rgb(122, 189, 205)
        canvas.drawOval(RectF(128f, 378f, 368f, 490f), paint)
        paint.color = Color.argb(178, 232, 250, 248)
        canvas.drawOval(RectF(160f, 392f, 240f, 428f), paint)
        canvas.drawOval(RectF(260f, 426f, 348f, 462f), paint)
        paint.color = Color.rgb(83, 128, 92)
        canvas.drawOval(RectF(92f, 450f, 150f, 488f), paint)
        paint.color = Color.rgb(255, 221, 227)
        canvas.drawCircle(132f, 454f, 10f, paint)
        paint.color = Color.argb(120, 255, 255, 255)
        paint.strokeWidth = 6f
        paint.style = Paint.Style.STROKE
        for (i in 0..2) {
            val x = 246f + i * 35f
            val bob = sin(partyPulse * 1.5f + i).toFloat() * 6f
            canvas.drawArc(RectF(x, 338f + bob, x + 28f, 378f + bob), 210f, 120f, false, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawGardenRow(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(102, 130, 68)
        canvas.drawRoundRect(RectF(x - 8f, y - 8f, x + 185f, y + 74f), 22f, 22f, paint)
        paint.color = Color.rgb(86, 122, 64)
        for (i in 0..5) {
            canvas.drawOval(RectF(x + i * 30f, y, x + 24f + i * 30f, y + 58f), paint)
            paint.color = if (i % 2 == 0) Color.rgb(244, 177, 100) else Color.rgb(116, 190, 119)
            canvas.drawCircle(x + 12f + i * 30f, y + 22f, 8f, paint)
            paint.color = Color.rgb(86, 122, 64)
        }
    }

    private fun drawSnackStall(canvas: Canvas, x: Float, y: Float) {
        drawShadow(canvas, x + 88f, y + 170f, 100f, 24f)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(204, 122, 106)
        canvas.drawRoundRect(RectF(x, y + 70f, x + 176f, y + 172f), 16f, 16f, paint)
        paint.color = Color.rgb(122, 72, 61)
        canvas.drawRect(x + 18f, y + 118f, x + 158f, y + 172f, paint)
        paint.color = Color.rgb(255, 229, 168)
        for (i in 0..3) {
            canvas.drawRoundRect(RectF(x - 8f + i * 48f, y + 56f, x + 40f + i * 48f, y + 92f), 10f, 10f, paint)
            paint.color = if (i % 2 == 0) Color.rgb(245, 153, 121) else Color.rgb(255, 229, 168)
        }
        paint.color = Color.rgb(86, 57, 44)
        canvas.drawRect(x + 24f, y + 34f, x + 32f, y + 172f, paint)
        canvas.drawRect(x + 144f, y + 34f, x + 152f, y + 172f, paint)
        paint.color = Color.rgb(255, 245, 215)
        canvas.drawCircle(x + 58f, y + 140f, 13f, paint)
        paint.color = Color.rgb(183, 66, 90)
        canvas.drawCircle(x + 96f, y + 140f, 13f, paint)
        paint.color = Color.rgb(229, 193, 91)
        canvas.drawCircle(x + 130f, y + 140f, 13f, paint)
    }

    private fun drawBurrow(canvas: Canvas, x: Float, y: Float) {
        drawShadow(canvas, x + 94f, y + 72f, 116f, 20f)
        paint.color = Color.rgb(127, 91, 57)
        canvas.drawOval(RectF(x, y, x + 190f, y + 88f), paint)
        paint.color = Color.rgb(65, 48, 39)
        canvas.drawOval(RectF(x + 35f, y + 18f, x + 155f, y + 86f), paint)
        paint.color = Color.rgb(179, 133, 79)
        canvas.drawOval(RectF(x - 18f, y + 42f, x + 208f, y + 104f), paint)
        paint.color = Color.rgb(105, 79, 54)
        canvas.drawOval(RectF(x + 42f, y + 34f, x + 148f, y + 100f), paint)
    }

    private fun drawTree(canvas: Canvas, x: Float, y: Float, size: Float) {
        drawShadow(canvas, x, y + 90f * size, 50f * size, 12f * size)
        paint.color = Color.rgb(113, 78, 49)
        canvas.drawRoundRect(RectF(x - 12f * size, y + 34f * size, x + 12f * size, y + 92f * size), 8f, 8f, paint)
        paint.color = Color.rgb(72, 132, 83)
        canvas.drawCircle(x - 24f * size, y + 34f * size, 34f * size, paint)
        paint.color = Color.rgb(88, 153, 92)
        canvas.drawCircle(x + 22f * size, y + 30f * size, 38f * size, paint)
        paint.color = Color.rgb(99, 166, 100)
        canvas.drawCircle(x, y, 42f * size, paint)
    }

    private fun drawBush(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(79, 139, 82)
        canvas.drawCircle(x - 22f, y + 10f, 28f, paint)
        canvas.drawCircle(x + 12f, y, 33f, paint)
        canvas.drawCircle(x + 42f, y + 14f, 24f, paint)
        paint.color = Color.rgb(255, 216, 142)
        canvas.drawCircle(x + 18f, y - 8f, 5f, paint)
        canvas.drawCircle(x - 18f, y + 2f, 5f, paint)
    }

    private fun drawFlowerPatch(canvas: Canvas, x: Float, y: Float) {
        for (i in 0..7) {
            val fx = x + (i % 4) * 26f
            val fy = y + (i / 4) * 28f
            paint.color = Color.rgb(70, 132, 75)
            canvas.drawLine(fx, fy + 12f, fx, fy + 28f, paint)
            paint.color = if (i % 2 == 0) Color.rgb(255, 210, 97) else Color.rgb(237, 137, 159)
            canvas.drawCircle(fx, fy + 10f, 7f, paint)
        }
    }

    private fun drawShadow(canvas: Canvas, x: Float, y: Float, rx: Float, ry: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(42, 49, 44, 33)
        canvas.drawOval(RectF(x - rx, y - ry, x + rx, y + ry), paint)
    }

    private fun drawSnacks(canvas: Canvas) {
        game.snacksOnMap.forEach { snack ->
            if (!snack.isCollected) {
                drawShadow(canvas, snack.x, snack.y + 17f, 17f, 5f)
                paint.style = Paint.Style.FILL
                when (snack.kind) {
                    "berry" -> drawBerry(canvas, snack.x, snack.y)
                    "seed" -> drawSeed(canvas, snack.x, snack.y)
                    "carrot" -> drawCarrot(canvas, snack.x, snack.y)
                    "mint" -> drawMint(canvas, snack.x, snack.y)
                    else -> drawClover(canvas, snack.x, snack.y)
                }
            }
        }
    }

    private fun drawBerry(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(183, 66, 90)
        canvas.drawCircle(x - 7f, y + 3f, 12f, paint)
        canvas.drawCircle(x + 7f, y + 2f, 13f, paint)
        canvas.drawCircle(x, y - 8f, 11f, paint)
        paint.color = Color.rgb(255, 218, 226)
        canvas.drawCircle(x - 4f, y - 9f, 4f, paint)
    }

    private fun drawSeed(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(232, 195, 92)
        canvas.drawOval(RectF(x - 17f, y - 10f, x + 17f, y + 14f), paint)
        paint.color = Color.rgb(143, 101, 55)
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        canvas.drawArc(RectF(x - 10f, y - 7f, x + 13f, y + 12f), 200f, 110f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawCarrot(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(82, 151, 82)
        canvas.drawOval(RectF(x - 15f, y - 24f, x, y - 4f), paint)
        canvas.drawOval(RectF(x, y - 24f, x + 15f, y - 4f), paint)
        paint.color = Color.rgb(229, 127, 57)
        val carrot = Path().apply {
            moveTo(x - 16f, y - 4f)
            lineTo(x + 16f, y - 4f)
            lineTo(x + 2f, y + 25f)
            close()
        }
        canvas.drawPath(carrot, paint)
    }

    private fun drawMint(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(83, 164, 105)
        canvas.drawOval(RectF(x - 22f, y - 16f, x + 2f, y + 10f), paint)
        canvas.drawOval(RectF(x - 2f, y - 17f, x + 22f, y + 9f), paint)
        paint.color = Color.rgb(198, 234, 177)
        canvas.drawLine(x, y - 12f, x, y + 14f, paint)
    }

    private fun drawClover(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(102, 166, 76)
        canvas.drawCircle(x - 9f, y - 7f, 10f, paint)
        canvas.drawCircle(x + 9f, y - 7f, 10f, paint)
        canvas.drawCircle(x, y + 6f, 10f, paint)
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(x, y + 10f, x - 8f, y + 24f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawNpcs(canvas: Canvas) {
        game.npcs.sortedBy { it.y }.forEach { npc ->
            val muted = npc.isHelped
            drawShadow(canvas, npc.x + 4f, npc.y + 44f, 68f, 16f)
            when (npc.species) {
                GameModel.Species.CAPYBARA -> drawCapybara(canvas, npc.x, npc.y, muted)
                GameModel.Species.GUINEA_PIG -> drawGuineaPig(canvas, npc.x, npc.y, muted)
                GameModel.Species.GERBIL -> drawGerbil(canvas, npc.x, npc.y, muted)
            }
            if (!npc.isHelped) {
                paint.color = Color.rgb(255, 248, 207)
                canvas.drawRoundRect(RectF(npc.x + 26f, npc.y - 92f, npc.x + 118f, npc.y - 44f), 18f, 18f, paint)
                paint.color = Color.rgb(117, 86, 54)
                canvas.drawCircle(npc.x + 45f, npc.y - 68f, 13f, paint)
                textPaint.color = Color.rgb(76, 55, 36)
                textPaint.textSize = 21f
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(npc.requestKind, npc.x + 82f, npc.y - 61f, textPaint)
            }
        }
    }

    private fun drawCapybara(canvas: Canvas, x: Float, y: Float, helped: Boolean) {
        val body = if (helped) Color.rgb(151, 119, 78) else Color.rgb(130, 94, 56)
        val face = if (helped) Color.rgb(128, 95, 62) else Color.rgb(101, 70, 42)
        paint.style = Paint.Style.FILL
        paint.color = body
        canvas.drawOval(RectF(x - 82f, y - 42f, x + 80f, y + 52f), paint)
        paint.color = Color.argb(70, 255, 223, 168)
        canvas.drawOval(RectF(x - 46f, y - 22f, x + 48f, y + 42f), paint)
        paint.color = face
        canvas.drawOval(RectF(x + 28f, y - 52f, x + 100f, y + 24f), paint)
        paint.color = body
        canvas.drawCircle(x + 42f, y - 50f, 13f, paint)
        canvas.drawCircle(x + 78f, y - 48f, 12f, paint)
        paint.color = Color.rgb(244, 231, 204)
        canvas.drawRoundRect(RectF(x - 48f, y - 48f, x + 22f, y - 20f), 13f, 13f, paint)
        paint.color = Color.rgb(238, 169, 110)
        canvas.drawCircle(x - 18f, y - 39f, 8f, paint)
        paint.color = Color.rgb(255, 230, 121)
        canvas.drawCircle(x - 18f, y - 43f, 5f, paint)
        drawFace(canvas, x + 66f, y - 14f, 11f)
        paint.color = Color.rgb(58, 43, 31)
        canvas.drawOval(RectF(x + 76f, y + 1f, x + 94f, y + 12f), paint)
    }

    private fun drawGuineaPig(canvas: Canvas, x: Float, y: Float, helped: Boolean) {
        paint.style = Paint.Style.FILL
        paint.color = if (helped) Color.rgb(218, 179, 138) else Color.rgb(239, 195, 145)
        canvas.drawOval(RectF(x - 58f, y - 43f, x + 58f, y + 46f), paint)
        paint.color = Color.rgb(255, 247, 225)
        canvas.drawOval(RectF(x - 30f, y - 39f, x + 40f, y + 39f), paint)
        paint.color = Color.rgb(174, 111, 75)
        canvas.drawCircle(x - 38f, y - 17f, 15f, paint)
        canvas.drawCircle(x + 37f, y - 18f, 13f, paint)
        paint.color = Color.rgb(245, 145, 139)
        canvas.drawCircle(x + 20f, y + 9f, 12f, paint)
        paint.color = Color.rgb(84, 56, 39)
        canvas.drawRoundRect(RectF(x - 45f, y + 30f, x + 48f, y + 45f), 8f, 8f, paint)
        paint.color = Color.rgb(255, 229, 155)
        canvas.drawRoundRect(RectF(x - 40f, y + 10f, x + 46f, y + 33f), 10f, 10f, paint)
        drawFace(canvas, x + 18f, y - 9f, 8f)
    }

    private fun drawGerbil(canvas: Canvas, x: Float, y: Float, helped: Boolean) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.rgb(126, 86, 52)
        val tail = Path().apply {
            moveTo(x - 38f, y + 14f)
            cubicTo(x - 78f, y + 18f, x - 86f, y + 60f, x - 124f, y + 50f)
        }
        canvas.drawPath(tail, paint)
        paint.style = Paint.Style.FILL
        paint.color = if (helped) Color.rgb(189, 143, 92) else Color.rgb(211, 158, 95)
        canvas.drawOval(RectF(x - 48f, y - 28f, x + 46f, y + 34f), paint)
        paint.color = Color.rgb(232, 185, 121)
        canvas.drawOval(RectF(x - 8f, y - 18f, x + 40f, y + 28f), paint)
        paint.color = Color.rgb(120, 82, 50)
        canvas.drawCircle(x + 29f, y - 28f, 15f, paint)
        paint.color = Color.rgb(218, 162, 104)
        canvas.drawCircle(x + 18f, y - 38f, 9f, paint)
        paint.color = Color.rgb(88, 59, 38)
        canvas.drawRoundRect(RectF(x - 42f, y - 56f, x + 14f, y - 36f), 8f, 8f, paint)
        paint.color = Color.rgb(246, 221, 139)
        canvas.drawCircle(x - 24f, y - 46f, 7f, paint)
        drawFace(canvas, x + 30f, y - 7f, 6f)
    }

    private fun drawFace(canvas: Canvas, x: Float, y: Float, eye: Float) {
        paint.color = Color.rgb(38, 30, 24)
        canvas.drawCircle(x, y, eye / 2f, paint)
        canvas.drawCircle(x + 18f, y + 1f, eye / 2.3f, paint)
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawArc(RectF(x - 4f, y + 8f, x + 26f, y + 25f), 15f, 150f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawPlayer(canvas: Canvas) {
        val x = game.playerX
        val y = game.playerY
        drawShadow(canvas, x, y + 34f, 31f, 9f)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(79, 125, 92)
        canvas.drawOval(RectF(x - 30f, y - 35f, x + 31f, y + 40f), paint)
        paint.color = Color.rgb(100, 151, 111)
        canvas.drawRoundRect(RectF(x - 24f, y - 2f, x + 24f, y + 38f), 14f, 14f, paint)
        paint.color = Color.rgb(248, 222, 168)
        canvas.drawCircle(x + 4f, y - 38f, 24f, paint)
        paint.color = Color.rgb(96, 67, 45)
        canvas.drawOval(RectF(x - 16f, y - 61f, x + 18f, y - 38f), paint)
        canvas.drawOval(RectF(x - 12f, y - 60f, x + 35f, y - 42f), paint)
        paint.color = Color.rgb(44, 48, 37)
        canvas.drawCircle(x - 5f, y - 40f, 4f, paint)
        canvas.drawCircle(x + 13f, y - 39f, 4f, paint)
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        canvas.drawArc(RectF(x - 5f, y - 32f, x + 18f, y - 20f), 20f, 140f, false, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 238, 178)
        canvas.drawRoundRect(RectF(x - 34f, y - 8f, x - 14f, y + 22f), 8f, 8f, paint)
    }

    private fun drawUi(canvas: Canvas) {
        uiPaint.style = Paint.Style.FILL
        uiPaint.color = Color.rgb(244, 225, 190)
        canvas.drawRect(0f, 0f, width.toFloat(), 128f, uiPaint)
        uiPaint.color = Color.argb(46, 61, 48, 31)
        canvas.drawRoundRect(RectF(14f, 14f, width - 14f, 118f), 24f, 24f, uiPaint)
        uiPaint.color = Color.rgb(255, 249, 229)
        canvas.drawRoundRect(RectF(10f, 8f, width - 18f, 112f), 24f, 24f, uiPaint)
        uiPaint.color = Color.rgb(111, 86, 55)
        canvas.drawRoundRect(RectF(24f, 26f, 128f, 94f), 20f, 20f, uiPaint)
        textPaint.color = Color.WHITE
        textPaint.textSize = 25f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("${game.happiness}", 43f, 55f, textPaint)
        textPaint.textSize = 18f
        canvas.drawText("care", 42f, 82f, textPaint)
        drawMiniSnackStrip(canvas, 146f, 31f)

        textPaint.color = Color.rgb(59, 47, 32)
        textPaint.textSize = 25f
        canvas.drawText(fitText(game.objectiveText, width - 178f, textPaint), 146f, 78f, textPaint)
        val toast = game.toast
        if (toast.isNotBlank()) {
            textPaint.color = Color.rgb(111, 86, 55)
            textPaint.textSize = 20f
            canvas.drawText(fitText(toast, width - 178f, textPaint), 146f, 103f, textPaint)
        }
        drawMomentCard(canvas)

        val bottom = height.toFloat()
        uiPaint.color = Color.rgb(244, 225, 190)
        canvas.drawRect(0f, bottom - 134f, width.toFloat(), bottom, uiPaint)
        helpButton.set(18f, bottom - 108f, width * 0.42f, bottom - 28f)
        pauseButton.set(width * 0.45f, bottom - 108f, width * 0.72f, bottom - 28f)
        resetButton.set(width * 0.75f, bottom - 108f, width - 18f, bottom - 28f)
        drawButton(canvas, helpButton, "Care")
        drawButton(canvas, pauseButton, if (game.isPaused) "Resume" else "Pause")
        drawButton(canvas, resetButton, "Reset")

        if (game.hasWon()) {
            uiPaint.color = Color.argb(222, 255, 248, 224)
            canvas.drawRoundRect(RectF(34f, height * 0.39f, width - 34f, height * 0.58f), 30f, 30f, uiPaint)
            uiPaint.color = Color.rgb(232, 178, 85)
            canvas.drawRoundRect(RectF(52f, height * 0.405f, width - 52f, height * 0.426f), 8f, 8f, uiPaint)
            textPaint.color = Color.rgb(72, 54, 37)
            textPaint.textSize = 38f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Sanctuary cozy!", width / 2f, height * 0.48f, textPaint)
            textPaint.textSize = 27f
            canvas.drawText("Enrichment done. Residents safe.", width / 2f, height * 0.525f, textPaint)
            textPaint.textSize = 23f
            canvas.drawText("Reset for another round.", width / 2f, height * 0.555f, textPaint)
        }
    }

    private fun drawButton(canvas: Canvas, rect: RectF, label: String) {
        uiPaint.color = Color.argb(48, 48, 40, 28)
        canvas.drawRoundRect(RectF(rect.left, rect.top + 5f, rect.right, rect.bottom + 5f), 18f, 18f, uiPaint)
        uiPaint.color = if (label == "Reset") Color.rgb(132, 99, 72) else Color.rgb(79, 133, 88)
        canvas.drawRoundRect(rect, 14f, 14f, uiPaint)
        uiPaint.color = Color.argb(46, 255, 255, 255)
        canvas.drawRoundRect(RectF(rect.left + 8f, rect.top + 7f, rect.right - 8f, rect.top + 23f), 8f, 8f, uiPaint)
        textPaint.color = Color.WHITE
        textPaint.textSize = if (label == "Resume") 25f else 28f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(label, rect.centerX(), rect.centerY() + 10f, textPaint)
        textPaint.color = Color.rgb(65, 52, 37)
    }

    private fun drawMomentCard(canvas: Canvas) {
        val top = 124f
        val left = 16f
        val right = width - 16f
        uiPaint.color = Color.argb(48, 61, 48, 31)
        canvas.drawRoundRect(RectF(left, top + 4f, right, top + 62f), 18f, 18f, uiPaint)
        uiPaint.color = Color.rgb(255, 246, 218)
        canvas.drawRoundRect(RectF(left, top, right, top + 58f), 18f, 18f, uiPaint)
        uiPaint.color = Color.rgb(92, 139, 103)
        canvas.drawRoundRect(RectF(left + 12f, top + 13f, left + 50f, top + 45f), 12f, 12f, uiPaint)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 238, 178)
        canvas.drawCircle(left + 31f, top + 29f, 9f, paint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.rgb(72, 54, 37)
        textPaint.textSize = 18f
        val speaker = game.currentResidentMomentSpeaker
        val personality = game.currentResidentMomentPersonality
        canvas.drawText(fitText("$speaker - $personality", right - left - 74f, textPaint), left + 62f, top + 24f, textPaint)
        textPaint.color = Color.rgb(61, 72, 49)
        textPaint.textSize = 20f
        canvas.drawText(fitText(game.currentResidentMomentText, right - left - 74f, textPaint), left + 62f, top + 48f, textPaint)
    }

    private fun drawMiniSnackStrip(canvas: Canvas, x: Float, y: Float) {
        val kinds = listOf("berry", "seed", "mint")
        kinds.forEachIndexed { index, kind ->
            val cx = x + index * 66f
            val count = game.getSnackCount(kind)
            uiPaint.color = if (count > 0) Color.rgb(255, 244, 205) else Color.rgb(232, 215, 185)
            canvas.drawRoundRect(RectF(cx, y, cx + 56f, y + 28f), 14f, 14f, uiPaint)
            paint.style = Paint.Style.FILL
            when (kind) {
                "berry" -> {
                    paint.color = Color.rgb(183, 66, 90)
                    canvas.drawCircle(cx + 18f, y + 14f, 7f, paint)
                }
                "seed" -> {
                    paint.color = Color.rgb(232, 195, 92)
                    canvas.drawOval(RectF(cx + 11f, y + 7f, cx + 27f, y + 21f), paint)
                }
                else -> {
                    paint.color = Color.rgb(83, 164, 105)
                    canvas.drawOval(RectF(cx + 10f, y + 7f, cx + 30f, y + 21f), paint)
                }
            }
            textPaint.color = Color.rgb(69, 54, 35)
            textPaint.textSize = 18f
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("x$count", cx + 31f, y + 20f, textPaint)
        }
    }

    private fun drawTownParty(canvas: Canvas) {
        val colors = intArrayOf(
            Color.rgb(240, 102, 112),
            Color.rgb(255, 205, 91),
            Color.rgb(93, 174, 125),
            Color.rgb(111, 156, 210)
        )
        paint.style = Paint.Style.FILL
        for (i in 0 until 18) {
            val x = ((i * 97) % 820 + 40).toFloat()
            val y = ((i * 149) % 1050 + 90).toFloat() + sin(partyPulse * 2.2f + i).toFloat() * 10f
            paint.color = colors[i % colors.size]
            canvas.drawRoundRect(RectF(x, y, x + 18f, y + 10f), 4f, 4f, paint)
        }
        paint.color = Color.rgb(255, 230, 137)
        canvas.drawCircle(600f, 575f, 18f + sin(partyPulse * 3f).toFloat() * 3f, paint)
        canvas.drawCircle(285f, 392f, 15f + sin(partyPulse * 2.5f).toFloat() * 2f, paint)
    }

    private fun fitText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) {
            return text
        }
        var end = text.length
        while (end > 4 && paint.measureText(text.substring(0, end) + "...") > maxWidth) {
            end -= 1
        }
        return text.substring(0, end).trimEnd() + "..."
    }
}
