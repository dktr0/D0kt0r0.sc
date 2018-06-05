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
			"8M",
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
			Server.default.options.recChannels = Server.local.options.numOutputBusChannels;
			Server.default.prepareForRecord;
			fork { 1.wait; Server.default.record; };
		});
	}

	*test {
		| nchnls |
		if(nchnls.isNil,{nchnls=Server.local.options.numOutputBusChannels});
		Pdef(\test,Pbind(
			\instrument,\point,
			\dur,0.25,
			\out,Pseq((0..(nchnls-1)),inf)
		)).play;
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

	*tricks {

		~instr = {
			| x = (\default) |
			Pbind(\instrument,x);
		};

		~transpose= {
			| x = ([0]) |
			Pbind(\midinote,Pkey(\midinote)+x);
		};

		~db = {
			| x = 0 |
			Pbind(\db,Pkey(\db)+x);
		};

		// this one doesn't work for some reason...
		~add = {
			| key = \midinote, x = ([0]) |
			Pbind(key.asSymbol,Pkey(key.asSymbol)+x);
		};

		~phrase = {
			| x = \phrase |
			Pbind(\type,\phrase,\instrument,x);
		};

		~centre = 1;
		~front = Pxrand([0,1,2],inf);
		~back = Pxrand([4,5,6],inf);
		~down = Pxrand((0..7),inf);
		~up = Pxrand((8..15),inf);
		~all = Pxrand((0..15),inf);
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
