D0kt0r0 {

	*boot {
		|numChannels=2,numInputs=2,sampleRate = 48000|
		Server.local.options.numOutputBusChannels = numChannels;
		Server.local.options.numInputBusChannels = numInputs;
		Server.local.options.numBuffers = 16384;
		Server.local.options.memSize = 1024*512;
		Server.local.options.sampleRate = sampleRate;
		Server.default = Server.local;
		Platform.case(
			\windows, {D0kt0r0.windowsDeviceSelection},
			\osx,{D0kt0r0.osxDeviceSelection},
			\linux,{D0kt0r0.osxDeviceSelection}
		);
		D0kt0r0.waitForBoot;
	}

	*windowsDeviceSelection {
		Server.local.options.device = "MOTU Audio ASIO";
	}

	*osxDeviceSelection {
		var devices = [
			"iRig PRO DUO",
			"Stage-B16",
			"Quartet",
			"MOTU UltraLite mk3 Hybrid",

			"MOTU 828mk3 Hybrid",
			"PreSonus FireStudio"].asSet;
		var matches = ServerOptions.devices.sect(devices);
		if(not(matches.isEmpty),{
			Server.local.options.device = matches[0];
		},{
			Server.local.options.inDevice = "Built-in Input";
			Server.local.options.outDevice = "Built-in Output";
		});
	}

	*waitForBoot {
		Server.default.waitForBoot( {
			D0kt0r0.synths;
			Server.default.prepareForRecord;
			fork { 1.wait; Server.default.record; };
		});
	}

	*stop {
		Server.default.stopRecording;
		fork { 1.wait; Server.default.quit; }
	}

	*synths {
		thisProcess.interpreter.executeFile((Platform.userExtensionDir ++ "/d0kt0r0.sc/synths.scd").standardizePath);
	}

	*superDirt {
		~dirt = SuperDirt(Server.local.options.numOutputBusChannels, Server.local);
		~dirt.loadSoundFiles;
		~dirt.start(57120, [0, 0]);
	}

}

+ Integer {
	db {
		^this.dbamp;
	}
}

+ Float {
	db {
		^this.dbamp;
	}
}
