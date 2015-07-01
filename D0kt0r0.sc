D0kt0r0 {

	*boot {
		|numChannels=2,sampleRate = 48000|
		var devices = [
			"MOTU UltraLite mk3 Hybrid",
			"MOTU 828mk3 Hybrid",
			"PreSonus FireStudio"].asSet;
		var matches = ServerOptions.devices.sect(devices);
		if(not(matches.isEmpty),{ Server.internal.options.device = matches[0]; });
		Server.internal.options.numOutputBusChannels = numChannels;
		Server.internal.options.memSize = 1024*512;
		Server.internal.options.sampleRate = sampleRate;
		Server.default = Server.internal;
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
		thisProcess.interpreter.executeFile("~/d0kt0r0.sc/synths.scd".standardizePath);
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
