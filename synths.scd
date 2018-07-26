// d0kt0r0's SynthDefs

(

SynthDef(\saw3,{
	| freq,amp,out,gate=0.5,attack=0.25,release=0.25,detune=0.01 |
	var audio = Saw.ar(8.collect({Rand(1-detune,1/(1-detune))})*freq,[0,-3,-6].dbamp*amp);
	audio = LPF.ar(audio,1000);
	audio = Mix.ar(audio);
	audio = audio * EnvGen.ar(Env.asr(attack,releaseTime:release),gate:gate,doneAction:2);
	Out.ar(out,audio);
}).add;

SynthDef(\sawResonz,{
	| freq = 55, amp = 0.1, out = 0 |
	var audio = Saw.ar(freq,mul:amp);
	audio = audio * EnvGen.ar(Env.perc(0.002,0.04),doneAction:2);
	audio = Resonz.ar(audio,freq:freq*[1,2,4,8,16,32,64,128],bwr:-10.dbamp,mul:10.dbamp);
	audio = Mix.ar(audio);
	Out.ar(out,audio);
}).add;

SynthDef(\pulseResonz,{
	| freq = 55, amp = 0.1, out = 0 |
	var audio = Pulse.ar(freq,mul:amp);
	audio = audio * EnvGen.ar(Env.perc(0.002,0.04),doneAction:2);
	audio = Resonz.ar(audio,freq:freq*[1,2,4,8,16,32,64,128],bwr:-10.dbamp,mul:10.dbamp);
	audio = Mix.ar(audio);
	Out.ar(out,audio);
}).add;

SynthDef(\resonzSnare,{
	| amp = 0.1, out = 0 |
	var audio = WhiteNoise.ar(mul:amp);
	audio = audio * EnvGen.ar(Env.perc(0.001,0.05),doneAction:2);
	audio = Resonz.ar(audio,freq:55*(1..20),bwr:-10.dbamp,mul:(20..1).dbamp*(-15.dbamp));
	audio = Mix.ar(audio);
	Out.ar(out,audio);
}).add;

SynthDef(\rest,{
	arg sustain=0.1;
	EnvGen.ar(Env.new([0,1],[sustain]),doneAction:2);
	Out.ar(0,0);
}).add;

SynthDef(\g,
	{
		arg quot=1,freq=440,amp=0.1,out=0,gate=1,lpf=5000,bwr=0.001;
		var audio,env,rfreq;
		freq = Clip.kr(freq,10,2640);
		rfreq = freq/quot;
		amp = amp * (20.dbamp) * ((bwr.ampdb*(-0.85)).dbamp);
		audio = Saw.ar(freq,amp) * Resonz.ar(PinkNoise.ar(),freq:freq/quot,mul:0.dbamp,bwr:bwr);
		env = EnvGen.ar(Env.adsr(0.001,0.001,-3.dbamp,0.025),gate:gate,doneAction:2);
		audio = audio*env;
		audio = LPF.ar(audio,freq:lpf);
		audio = Compander.ar(audio,audio,thresh:-3.dbamp,slopeAbove:0.05,clampTime:0.001);
		Out.ar(out,audio);
	}
).add;

SynthDef(\rezon,{
    arg freq=440,amp=0.1,gate=1.0,out=0;
    var audio,env;
	freq = Clip.kr(freq,10,3520);
	amp = Clip.kr(amp,0,2);
	amp = amp * (30.dbamp);
    audio = WhiteNoise.ar(mul:amp);
	audio = audio + Saw.ar(freq,mul:amp*(-15.dbamp));
	audio = Resonz.ar(audio,freq,bwr:0.005);
	env = Env.adsr(0.1,0.1,-1.dbamp,0.75,curve:0.9);
    audio = audio * EnvGen.ar(env,doneAction:2,gate:gate);
	audio = Compander.ar(audio,audio,-3.dbamp,slopeAbove:0.05,clampTime:0.01,relaxTime:0.3);
    Out.ar(out,audio);
}).add;

SynthDef(\rezonSwell,{
    arg freq=440,amp=0.1,sustain=1.0,out=0;
    var audio,env;
	freq = Clip.kr(freq,10,3520);
	amp = Clip.kr(amp,0,2);
	amp = amp * (30.dbamp);
    audio = WhiteNoise.ar(mul:amp);
	audio = audio + Saw.ar(freq,mul:amp*(-15.dbamp));
	audio = Resonz.ar(audio,freq,bwr:0.005);
	env = Env.linen(sustain*2/5,sustain*1/5,sustain*2/5);
    audio = audio * EnvGen.ar(env,doneAction:2);
	audio = Compander.ar(audio,audio,-3.dbamp,slopeAbove:0.05,clampTime:0.01,relaxTime:0.3);
    Out.ar(out,audio);
}).add;

SynthDef(\nov05,{
    arg freq=440,amp=0.1,sustain=1.0,out=0,lpf=1000;
    var audio,env;
	freq = Clip.kr(freq,10,3000);
    sustain = Clip.kr(sustain,0.201,1000);
    audio = Pulse.ar(freq:freq,mul:amp);
    audio = LPF.ar(audio,freq:lpf);
    env = Env.new([0,1,0],[sustain/2,sustain/2]);
    audio = audio * EnvGen.ar(env,doneAction:2);
    Out.ar(out,audio);
}).add;

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
		arg freq=440,amp=0.1,gate=0.5,out=0,lpf=14000,attack=0.001,mul=1;
		var mod,car,env;
		mod = Env.pairs([[0,12],[0.4,0.5]],-8);
		mod = EnvGen.ar(mod);
		mod = SinOsc.ar(freq*12.8,mul:freq*mod,add:freq*mul);
		car = SinOsc.ar(mod,mul:amp);
		env = Env.adsr(attack,0.01,-5.dbamp,0.1,curve:-4);
		env = EnvGen.ar(env,gate:gate,doneAction:2);
		car = LPF.ar(car*env,freq:lpf);
		car = car*SinOsc.ar(1,mul:0.1,add:1);
		Out.ar(out,car*env);
	}
).add;

SynthDef(\fm29,
	{
		arg freq=440,amp=0.1,gate=1,out=0,lpf=20000;
		var f,mod,car,env,aud;
		f = Clip.kr(freq,12.midicps,108.midicps)/2;
		mod = EnvGen.ar(Env.pairs([[0,14],[0.4,0.5]],-8));
		mod = SinOsc.ar(f*4,mul:f*mod); // modulating oscillator
		car = SinOsc.ar(f+mod); // carrier oscillator
		env = Env.adsr(0.001,0.008,-10.dbamp,0.008,curve:-4); // envelope
		aud = car*EnvGen.ar(env,gate:gate,doneAction:2)*3.dbamp;
		aud = Compander.ar(aud,aud,-10.dbamp,clampTime:0.001,relaxTime:0.001,slopeAbove:1/10); // compression
		aud = aud * (3).dbamp; // makeup gain
		aud = LPF.ar(aud,freq:Clip.kr(lpf,20,20000)); // lowpass filter
		aud = aud * Clip.kr(amp,0,12.dbamp); // overall gain
		Out.ar(out,aud);
	}
).add;

SynthDef(\revtine,
	{
		arg freq=440,amp=0.1,gate=0.5,out=0,lpf=14000,attack=0.001,mul=1;
		var mod,car,env;
		mod = Env.pairs([[0,12],[0.4,0.5]],-8);
		mod = EnvGen.ar(mod);
		mod = SinOsc.ar(freq*12.8,mul:freq*mod,add:freq*mul);
		car = SinOsc.ar(mod,mul:amp);
		env = Env.adsr(attack,0.01,-5.dbamp,0.1,curve:-4);
		env = 1 - EnvGen.ar(env,gate:gate,doneAction:2);
		car = LPF.ar(car*env,freq:lpf);
		car = car*SinOsc.ar(1,mul:0.1,add:1);
		Out.ar(out,car*env);
	}
).add;

c = Buffer.alloc(Server.default,16384);
c.cheby([1,1,1,1,0.5,0.2,0.5,0.1,0.1,0.1,0.1,0.1]);
SynthDef(\chebyBass,
	{
		arg freq=55,amp=0.1,out=0,preamp=1,gate=0.5,fnoise=0.003,lpf=8000;
		var audio = SinOsc.ar(freq*Rand(1-fnoise,1+fnoise),mul:preamp);
		audio = Shaper.ar(c.bufnum,audio,mul:amp);
		audio = audio * EnvGen.ar(Env.adsr(0.001,0.3,-3.dbamp,0.3),gate:gate,doneAction:2);
		audio = LPF.ar(audio,freq:lpf);
		Out.ar(out,audio);
	}
).add;

SynthDef(\cblip,
	{
		arg freq = 60, out = 0, amp = 0.1, sustain = 0.099, lpf=10000, preamp=1;
		var audio;
		audio = Blip.ar(freq, mul:preamp);
		audio = Shaper.ar(c.bufnum,audio,mul:amp);
		audio = audio * EnvGen.ar(Env.perc(0.001,sustain-0.001), doneAction: 2);
		audio = LPF.ar(audio,freq:lpf);
		Out.ar(out,audio);
}).add;

SynthDef(\click,
	{
		arg freq = 60, out = 0, amp = 0.1, sustain = 0.099, lpf=10000;
		var audio;
		audio = Impulse.ar(0,mul:amp);
		audio = audio * EnvGen.ar(Env.new([1,0],[0.050]), doneAction: 2);
		audio = LPF.ar(audio,freq:lpf);
		Out.ar(out,audio);
}).add;

SynthDef(\burst,
	{
		arg freq = 60, out = 0, amp = 0.1, sustain = 0.099, lpf=10000;
		var audio;
		audio = WhiteNoise.ar(mul:amp*(80.dbamp));
		audio = Resonz.ar(audio,freq:freq,bwr:0.01);
		audio = audio * EnvGen.ar(Env.new([1,0],[0.005]), doneAction: 2);
		audio = LPF.ar(audio,freq:lpf);
		Out.ar(out,audio);
}).add;

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
		arg in = 0, out = 0, sustain = 1.0, amp = 0.1;
		var audio = In.ar(in) * amp;
		audio = audio * EnvGen.ar(Env.linen(0.001,sustain-0.011,0.010),doneAction:2);
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
		arg freq=110,amp=0.1,gate=0.5,gain = 2,density=5000,out=0;
		var audio = Dust.ar(density,mul:amp);
		var env = Env.adsr(0.001,0.001,-3.dbamp,0.011);
		audio = MoogFF.ar(audio,freq,gain);
		audio = LPF.ar(audio,13500);
		audio = audio*EnvGen.ar(env,gate:gate,doneAction:2);
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
		arg freq = 60, out = 0, amp = 0.1, sustain = 0.099, lpf = 10000;
		var audio;
		audio = Blip.ar(freq, mul: amp);
		audio = audio * EnvGen.ar(Env.perc(0.001,sustain-0.001), doneAction: 2);
		audio = LPF.ar(audio,freq:lpf);
		Out.ar(out,audio);
}).add;

SynthDef(\splat,
	{
		arg freq = 60, out = 0, amp = 0.1, sustain = 0.051, lpf = 10000;
		var audio = WhiteNoise.ar(amp);
		audio = audio * EnvGen.ar(Env.perc(0.001,sustain - 0.001), doneAction: 2);
		audio = LPF.ar(audio,freq:lpf);
		Out.ar(out,audio);
}).add;

SynthDef(\ping,
	{
		arg freq = 60, out = 0, amp = 0.1, sustain = 0.051, pan = 0, fnoise = 0.003;
		var audio;
		audio = Saw.ar(freq*Rand(1-fnoise,1+fnoise),mul:amp);
		audio = audio * EnvGen.ar(Env.perc(0.001,sustain - 0.001), doneAction: 2);
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

SynthDef(\pianoSwell,
	{
		arg freq = 440, out = 0, amp = 0.1, lpf = 2500, sustain = 6.001, fnoise = 0.003;
		var env;
		var audio = LFTri.ar(freq*Rand(1-fnoise,1+fnoise), mul: amp);
		audio = LPF.ar(audio, freq: lpf);
		env = EnvGen.ar(Env.new([0,1,0.2,0],[0.020,0.020,sustain-0.040]),doneAction:2);
		audio = audio*env;
		Out.ar(out,audio);
}).add;

SynthDef(\sawswell,
	{
		arg freq = 440, out = 0, amp = 0.1, sustain = 6.001, fnoise = 0.003, lpf = 10000;
		var audio = Saw.ar(freq*Rand(1-fnoise,1+fnoise), mul: amp);
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
		audio = audio * EnvGen.ar(Env.adsr(0,0.2,-10.dbamp,10),gate,doneAction:2);
		Out.ar(out,audio);
}).add;

SynthDef(\saw2,
	{
		arg out=0,out2=1,pan=0,pan2=0,freq=440,sustain=1,amp=0.1,lpf = 5000, fnoise=0.003;
		var audio,panPos;
		audio = Lag.ar(K2A.ar(freq*Rand(1-fnoise,1+fnoise)),0.5);
		audio = Saw.ar(audio,mul:amp);
		audio = LPF.ar(audio,freq:lpf);
		audio = audio * EnvGen.ar(Env.new([0,1,-10.dbamp,-10.dbamp,0],[0.001,0.02,sustain-0.021,0.5]),doneAction:2);
		panPos = Line.kr(pan,pan2,sustain);
		audio = Pan2.ar(audio,panPos);
		Out.ar(out,audio[0]);
		Out.ar(out2,audio[1]);
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

)
