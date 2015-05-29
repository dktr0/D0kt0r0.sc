// d0kt0r0's SynthDefs

(

SynthDef(\shift,
	{
		arg in = 0, out = 0, amp = 0.1, delay = 1.0, sustain = 0.050;
		var audio = SoundIn.ar(in);
		audio = audio * EnvGen.ar(Env.linen(0.01,sustain-0.02,0.01));
		EnvGen.ar(Env.linen(0.01,delay+sustain-0.02,0.01),doneAction:2);
		audio = DelayN.ar(audio,delay,delay,mul:amp);
		Out.ar(out,audio);
	}
).add;

SynthDef(\pulse,
	{
		arg amp = 0.1,in = 0, out = 0,sustain = 0.050;
		var audio,env;
		audio = SoundIn.ar(in);
		env = EnvGen.ar(Env.perc(0.007,sustain-0.007),doneAction:2);
		Out.ar(out,audio*env*amp);
}).add;

SynthDef(\dlyPulse,
	{
		arg amp = 0.1,in = 0, out = 0,sustain = 0.050, dly = 0.050;
		var audio,env;
		audio = SoundIn.ar(in);
		env = EnvGen.ar(Env.perc(0.007,sustain-0.007));
		EnvGen.ar(Env.perc(0.007,sustain-0.007+dly),doneAction:2);
		audio = DelayN.ar(audio*env*amp,sustain + dly, sustain + dly);
		Out.ar(out,audio);
}).add;

SynthDef(\x,
	{
		arg amp = 0.1,out = 0,sustain = 0.050;
		var audio,env;
		audio = Ndef(\d1).ar;
		env = EnvGen.ar(Env.perc(0.001,sustain-0.001),doneAction:2);
		Out.ar(out,audio*env*amp);
}).add;

SynthDef(\tine,
	{
		arg freq=440,amp=0.1,gate=0.5,out=0,lpf=14000;
		var mod,car,env;
		mod = Env.pairs([[0,12],[0.4,0.5]],-8);
		mod = EnvGen.ar(mod);
		mod = SinOsc.ar(freq*12.8,mul:freq*mod,add:freq);
		car = SinOsc.ar(mod,mul:amp);
		env = Env.adsr(0.001,0.01,-5.dbamp,0.1,curve:-4);
		env = EnvGen.ar(env,gate:gate,doneAction:2);
		car = LPF.ar(car*env,freq:lpf);
		car = car*SinOsc.ar(1,mul:0.1,add:1);
		Out.ar(out,car*env);
	}
).add;

~chebyBassTransfer = Buffer.alloc(Server.default,16384);
~chebyBassTransfer.cheby([1,0,0.5,0,0.5,0.2,0.5,0.1,0.1,0.1,0.1,0.1]);
SynthDef(\chebyBass,
	{
		arg freq=55,amp=0.1,out=0,preamp=1,gate=0.5,fnoise=0.003,lpf=8000;
		var audio = SinOsc.ar(freq*Rand(1-fnoise,1+fnoise),mul:preamp);
		audio = Shaper.ar(~chebyBassTransfer.bufnum,audio,mul:amp);
		audio = audio * EnvGen.ar(Env.adsr(0.001,0.3,-3.dbamp,0.3),gate:gate,doneAction:2);
		audio = LPF.ar(audio,freq:lpf);
		Out.ar(out,audio);
	}
).add;

SynthDef(\fmSwell,
	{
		arg freq, index = 15, amp = 0.03, out = 0, hpf = 1000;
		var mod, audio;
		mod = SinOsc.ar(freq,mul:freq*index,add:freq);
		mod = mod + LFNoise0.ar(5,mul:10);
		audio = SinOsc.ar(mod,mul:amp);
		audio = HPF.ar(audio,hpf);
		audio = audio*EnvGen.ar(Env.linen(0.8,0.8,3.2),doneAction:2);
		Out.ar(out,audio);
	}
).add;

SynthDef(\zip,
	{
	arg freq=440,amp=0.1,out=0,gate=0.5,fnoise=0.003;
	var freqEnv,ampEnv,audio;
	freqEnv = Env.asr(0.050,1,1);
	freqEnv = ((freq*2)-EnvGen.ar(freqEnv,gate:gate,levelScale:freq))*Rand(1-fnoise,1+fnoise);
	ampEnv = Env.adsr(0.001,1.0,-6.dbamp,0.005);
	ampEnv = EnvGen.ar(ampEnv,levelScale:amp,gate:gate,doneAction:2);
	audio = Saw.ar(freqEnv) * ampEnv;
	Out.ar(out,audio);
	}
).add;

SynthDef(\w,
	{
		arg in = 0, out = 0, dur = 1.0, amp = 0.1;
		var audio = In.ar(in) * amp;
		audio = audio * EnvGen.ar(Env.linen(0.001,dur-0.011,0.010),doneAction:2);
		Out.ar(out,audio);
	}
).add;

SynthDef(\point,
	{
		arg freq = 440, amp = 0.1, out = 0;
		var audio = SinOsc.ar(freq,mul:amp);
		var env = Env.perc(0.005,0.100);
		audio = audio * EnvGen.ar(env,doneAction:2);
		Out.ar(out,audio);
}).add;

SynthDef(\dust,
	{
		arg freq=110,amp=0.1,dur=0.061,gain = 2,density=5000,out=0;
		var audio = Dust.ar(density,mul:amp);
		var env = Env.linen(0.001,0.010,dur-0.011);
		audio = MoogFF.ar(audio,freq,gain);
		audio = LPF.ar(audio,13500);
		audio = audio*EnvGen.ar(env,doneAction:2);
		Out.ar(out,audio);
}).add;

SynthDef(\kick,
	{
		arg freq = 60, out = 0, amp = 0.1, sustain = 0.101;
		var audio;
		audio = SinOsc.ar(freq, mul: amp);
		audio = audio * EnvGen.ar(Env.perc(0.001,sustain-0.001), doneAction: 2);
		Out.ar(out,audio);
}).add;

SynthDef(\blip,
	{
		arg freq = 60, out = 0, amp = 0.1, dur = 0.099;
		var audio;
		audio = Blip.ar(freq, mul: amp);
		audio = audio * EnvGen.ar(Env.perc(0.001,dur-0.001), doneAction: 2);
		Out.ar(out,audio);
}).add;

SynthDef(\splat,
	{
		arg freq = 60, out = 0, amp = 0.1, sustain = 0.051;
		var audio = PinkNoise.ar(amp);
		audio = audio * EnvGen.ar(Env.perc(0.001,sustain - 0.001), doneAction: 2);
		Out.ar(out,audio);
}).add;

SynthDef(\ping,
	{
		arg freq = 60, out = 0, amp = 0.1, dur = 0.051, pan = 0, fnoise = 0.003;
		var audio;
		audio = Saw.ar(freq*Rand(1-fnoise,1+fnoise),mul:amp);
		audio = audio * EnvGen.ar(Env.perc(0.001,dur - 0.001), doneAction: 2);
		Out.ar(out,audio);
}).add;

SynthDef(\swell,
	{
		arg freq = 440, out = 0, amp = 0.1, lpf = 2500, sustain = 6.001, fnoise = 0.003;
		var audio = LFTri.ar(freq*Rand(1-fnoise,1+fnoise), mul: amp);
		audio = LPF.ar(audio, freq: lpf);
		audio = audio*EnvGen.ar(Env.linen((sustain-0.001)*0.33,0.001,(sustain-0.001)*0.67), doneAction: 2);
		Out.ar(out,audio);
}).add;

SynthDef(\saw,
	{
		arg out=0,freq=440,gate=0.5,amp=0.1,lpf = 5000, fnoise=0.003;
		var audio;
		audio = Lag.ar(K2A.ar(freq*Rand(1-fnoise,1+fnoise)),0.5);
		audio = Saw.ar(audio,mul:amp);
		audio = LPF.ar(audio,freq:lpf);
		audio = audio * EnvGen.ar(Env.adsr(0,0.2,-10.dbamp,10),gate);
		Out.ar(out,audio);
}).add;

SynthDef(\sawU,
	{
		arg freq = 440, amp = 0.1, out = 0, gate = 0.5;
		var audio, env;
		env = Env.asr(0.050,releaseTime:0.5);
		env = EnvGen.ar(env,gate,doneAction:2);
		audio = Lag.ar(LFNoise0.ar(4,freq/50),0.5,add:freq);
		audio = Saw.ar(audio,mul:amp);
		audio = LPF.ar(audio,10000);
		audio = audio * env;
		Out.ar(out,audio);
	}
).add;

"synths loaded".postln;
)
