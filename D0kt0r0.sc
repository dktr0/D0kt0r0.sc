D0kt0r0 {

	*boot {
		|numChannels=2,sampleRate = 48000|
		var devices = [
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
		Server.local.options.numOutputBusChannels = numChannels;
		Server.local.options.memSize = 1024*512;
		Server.local.options.sampleRate = sampleRate;
		Server.default = Server.local;
		D0kt0r0.waitForBoot;
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
