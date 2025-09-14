package io.github.clentgame.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.IntervalSystem
import io.github.clentgame.event.EntityAttackEvent
import io.github.clentgame.event.EntityDeathEvent
import io.github.clentgame.event.EntityLootEvent
import io.github.clentgame.event.GamePauseEvent
import io.github.clentgame.event.GameResumeEvent
import io.github.clentgame.event.MapChangeEvent
import ktx.assets.disposeSafely
import ktx.log.logger
import ktx.tiled.propertyOrNull

class AudioSystem: EventListener, IntervalSystem() {

    private val musicCache = mutableMapOf<String, Music>()
    private val soundCache = mutableMapOf<String, Sound>()
    private val soundRequest = mutableMapOf<String, Sound>()
    private var music: Music? = null

    override fun onTick() {
        if(soundRequest.isEmpty()){
            return
        }
        soundRequest.values.forEach { it.play(1f) }
        soundRequest.clear()
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is MapChangeEvent -> {
                event.map.propertyOrNull<String>("music")?.let { path ->
                    log.debug { "Changing music to $path" }
                     val newMusic = musicCache.getOrPut(path){
                        Gdx.audio.newMusic(Gdx.files.internal(path)).apply {
                            isLooping = true
                        }
                    }
                    if (music != null && newMusic != music){
                        music?.stop()
                    }
                    music = newMusic
                    music?.play()
                }
                return true
            }
            is EntityAttackEvent -> queueSound("audio/${event.model.atlasKey}_attack.wav")
            is EntityDeathEvent -> queueSound("audio/${event.model.atlasKey}_death.wav")
            is EntityLootEvent -> queueSound("audio/${event.model.atlasKey}_open.wav")
            is GamePauseEvent -> {
                music?.pause()
                soundCache.values.forEach { it.pause() }
            }
            is GameResumeEvent -> {
                music?.play()
                soundCache.values.forEach { it.resume() }
            }

        }
        return false

    }
    private fun queueSound(soundPath: String){
        log.debug { "Queueing sound: $soundPath" }
        if (soundPath in soundRequest){
            //already queued -> do nothing
            return
        }

        val sound = soundCache.getOrPut(soundPath){
            Gdx.audio.newSound(Gdx.files.internal(soundPath))
        }
        soundRequest[soundPath] = sound

    }

    override fun onDispose() {
        musicCache.values.forEach{it.disposeSafely()}
        soundCache.values.forEach{it.disposeSafely()}
    }

    companion object{
        private val log = logger<AudioSystem>()
    }

}
