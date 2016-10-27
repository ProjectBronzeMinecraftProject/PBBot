package com.projectbronze.pbbot.sound;

import net.dv8tion.jda.audio.AudioReceiveHandler;
import net.dv8tion.jda.audio.CombinedAudio;
import net.dv8tion.jda.audio.UserAudio;
import net.dv8tion.jda.entities.User;

public class SoundListner implements AudioReceiveHandler
{

	@Override
	public boolean canReceiveCombined()
	{
		return true;
	}

	@Override
	public boolean canReceiveUser()
	{
		return false;
	}

	@Override
	public void handleCombinedAudio(CombinedAudio combinedAudio)
	{
		byte[] audio = combinedAudio.getAudioData(1.0);
	}

	@Override
	public void handleUserAudio(UserAudio userAudio){}

	@Override
	public void handleUserTalking(User user, boolean talking)
	{
	}

}
