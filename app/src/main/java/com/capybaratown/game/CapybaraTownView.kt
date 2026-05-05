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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.nanoTime()
        val dt = if (lastFrameNanos == 0L) 0f else (now - lastFrameNanos) / 1_000_000_000f
        lastFrameNanos = now
        game.tick(dt)

        calculateWorldTransform()
        canvas.drawColor(Color.rgb(247, 229, 190))
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)
        drawTown(canvas)
        drawSnacks(canvas)
        drawNpcs(canvas)
        drawPlayer(canvas)
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
        val availableHeight = height - 245f
        scale = min(width / GameModel.WORLD_WIDTH, availableHeight / GameModel.WORLD_HEIGHT)
        offsetX = (width - GameModel.WORLD_WIDTH * scale) / 2f
        offsetY = 116f
    }

    private fun drawTown(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(164, 200, 124)
        canvas.drawRect(0f, 0f, GameModel.WORLD_WIDTH, GameModel.WORLD_HEIGHT, paint)

        paint.color = Color.rgb(224, 192, 128)
        paint.strokeWidth = 52f
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

        paint.style = Paint.Style.FILL
        drawHouse(canvas, 145f, 220f, Color.rgb(176, 124, 82), Color.rgb(108, 83, 57))
        drawHouse(canvas, 620f, 405f, Color.rgb(207, 122, 111), Color.rgb(110, 81, 67))
        drawHouse(canvas, 455f, 910f, Color.rgb(154, 128, 197), Color.rgb(95, 79, 128))

        paint.color = Color.rgb(119, 173, 195)
        canvas.drawOval(RectF(105f, 365f, 390f, 500f), paint)
        paint.color = Color.rgb(224, 246, 248)
        canvas.drawOval(RectF(160f, 392f, 240f, 428f), paint)
        canvas.drawOval(RectF(260f, 422f, 345f, 460f), paint)

        paint.color = Color.rgb(91, 118, 66)
        for (i in 0..5) {
            canvas.drawOval(RectF(642f + i * 30f, 625f, 665f + i * 30f, 695f), paint)
        }
        paint.color = Color.rgb(112, 79, 51)
        canvas.drawOval(RectF(410f, 1030f, 600f, 1118f), paint)
    }

    private fun drawHouse(canvas: Canvas, x: Float, y: Float, wall: Int, roof: Int) {
        paint.style = Paint.Style.FILL
        paint.color = wall
        canvas.drawRoundRect(RectF(x, y + 55f, x + 165f, y + 165f), 18f, 18f, paint)
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
    }

    private fun drawSnacks(canvas: Canvas) {
        game.snacksOnMap.forEach { snack ->
            if (!snack.isCollected) {
                paint.style = Paint.Style.FILL
                paint.color = when (snack.kind) {
                    "berry" -> Color.rgb(185, 70, 95)
                    "seed" -> Color.rgb(238, 203, 106)
                    "carrot" -> Color.rgb(226, 128, 62)
                    "mint" -> Color.rgb(91, 170, 112)
                    else -> Color.rgb(118, 166, 73)
                }
                canvas.drawCircle(snack.x, snack.y, 18f, paint)
                paint.color = Color.rgb(255, 245, 202)
                canvas.drawCircle(snack.x - 5f, snack.y - 6f, 5f, paint)
            }
        }
    }

    private fun drawNpcs(canvas: Canvas) {
        game.npcs.forEach { npc ->
            val muted = npc.isHelped
            when (npc.species) {
                GameModel.Species.CAPYBARA -> drawCapybara(canvas, npc.x, npc.y, muted)
                GameModel.Species.GUINEA_PIG -> drawGuineaPig(canvas, npc.x, npc.y, muted)
                GameModel.Species.GERBIL -> drawGerbil(canvas, npc.x, npc.y, muted)
            }
            if (!npc.isHelped) {
                paint.color = Color.rgb(255, 244, 173)
                canvas.drawCircle(npc.x + 48f, npc.y - 50f, 20f, paint)
                textPaint.textSize = 28f
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText("!", npc.x + 48f, npc.y - 40f, textPaint)
            }
        }
    }

    private fun drawCapybara(canvas: Canvas, x: Float, y: Float, helped: Boolean) {
        paint.color = if (helped) Color.rgb(150, 121, 82) else Color.rgb(128, 95, 58)
        canvas.drawOval(RectF(x - 70f, y - 35f, x + 75f, y + 48f), paint)
        paint.color = if (helped) Color.rgb(120, 91, 60) else Color.rgb(91, 67, 45)
        canvas.drawOval(RectF(x + 28f, y - 46f, x + 92f, y + 25f), paint)
        drawFace(canvas, x + 62f, y - 12f, 11f)
    }

    private fun drawGuineaPig(canvas: Canvas, x: Float, y: Float, helped: Boolean) {
        paint.color = if (helped) Color.rgb(221, 181, 139) else Color.rgb(238, 194, 145)
        canvas.drawOval(RectF(x - 52f, y - 38f, x + 52f, y + 42f), paint)
        paint.color = Color.rgb(255, 246, 224)
        canvas.drawOval(RectF(x - 12f, y - 35f, x + 40f, y + 37f), paint)
        drawFace(canvas, x + 26f, y - 6f, 8f)
    }

    private fun drawGerbil(canvas: Canvas, x: Float, y: Float, helped: Boolean) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.rgb(131, 91, 56)
        canvas.drawLine(x - 36f, y + 10f, x - 88f, y + 42f, paint)
        paint.style = Paint.Style.FILL
        paint.color = if (helped) Color.rgb(189, 143, 92) else Color.rgb(211, 158, 95)
        canvas.drawOval(RectF(x - 42f, y - 25f, x + 44f, y + 32f), paint)
        paint.color = Color.rgb(120, 82, 50)
        canvas.drawCircle(x + 28f, y - 26f, 14f, paint)
        drawFace(canvas, x + 28f, y - 4f, 6f)
    }

    private fun drawFace(canvas: Canvas, x: Float, y: Float, eye: Float) {
        paint.color = Color.rgb(38, 30, 24)
        canvas.drawCircle(x, y, eye / 2f, paint)
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawArc(RectF(x - 18f, y + 8f, x + 18f, y + 28f), 15f, 150f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawPlayer(canvas: Canvas) {
        val x = game.playerX
        val y = game.playerY
        paint.color = Color.rgb(86, 128, 94)
        canvas.drawOval(RectF(x - 31f, y - 40f, x + 31f, y + 37f), paint)
        paint.color = Color.rgb(252, 231, 166)
        canvas.drawCircle(x + 16f, y - 20f, 15f, paint)
        paint.color = Color.rgb(55, 76, 55)
        canvas.drawCircle(x + 18f, y - 23f, 4f, paint)
    }

    private fun drawUi(canvas: Canvas) {
        uiPaint.style = Paint.Style.FILL
        uiPaint.color = Color.rgb(255, 247, 224)
        canvas.drawRect(0f, 0f, width.toFloat(), 112f, uiPaint)
        uiPaint.color = Color.rgb(98, 79, 55)
        canvas.drawRect(0f, 108f, width.toFloat(), 112f, uiPaint)

        textPaint.color = Color.rgb(65, 52, 37)
        textPaint.textSize = 34f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Happiness ${game.happiness}", 22f, 42f, textPaint)
        canvas.drawText("Snacks ${game.snacks}", 22f, 86f, textPaint)
        textPaint.textSize = 28f
        canvas.drawText(fitText(game.objectiveText, width - 300f, textPaint), 260f, 52f, textPaint)
        val toast = game.toast
        if (toast.isNotBlank()) {
            canvas.drawText(fitText(toast, width - 300f, textPaint), 260f, 88f, textPaint)
        }

        val bottom = height.toFloat()
        helpButton.set(22f, bottom - 108f, 202f, bottom - 28f)
        pauseButton.set(224f, bottom - 108f, 404f, bottom - 28f)
        resetButton.set(width - 184f, bottom - 108f, width - 22f, bottom - 28f)
        drawButton(canvas, helpButton, "Help")
        drawButton(canvas, pauseButton, if (game.isPaused) "Resume" else "Pause")
        drawButton(canvas, resetButton, "Reset")

        if (game.hasWon()) {
            uiPaint.color = Color.argb(210, 255, 247, 224)
            canvas.drawRoundRect(RectF(44f, height * 0.42f, width - 44f, height * 0.56f), 28f, 28f, uiPaint)
            textPaint.textSize = 38f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Town party!", width / 2f, height * 0.48f, textPaint)
            textPaint.textSize = 27f
            canvas.drawText("Reset to play another cozy round.", width / 2f, height * 0.525f, textPaint)
        }
    }

    private fun drawButton(canvas: Canvas, rect: RectF, label: String) {
        uiPaint.color = Color.rgb(91, 140, 90)
        canvas.drawRoundRect(rect, 14f, 14f, uiPaint)
        textPaint.color = Color.WHITE
        textPaint.textSize = 29f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(label, rect.centerX(), rect.centerY() + 10f, textPaint)
        textPaint.color = Color.rgb(65, 52, 37)
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
