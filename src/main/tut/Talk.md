# SpinalHDL

Programming FPGAs with Scala

_A talk by Jennifer Wilcox_

---

## What is SpinalHDL?

- A hardware description language (HDL)
- Used to program the internal logic layout of an FPGA (and ASICs too, but those are expensive!)
- Similar to VHDL and Verilog in functionality, but is based on Scala

---

## Whirlwind intro to FPGAs

<img class="plain" src="figures/fpga.png" />

- An FPGA is a collection of programmable logic elements
- You can arrange the logic elements to behave like discrete logic components
  wired together, or like the transistors in an ASIC

---

## Whirlwind intro to FPGAs

<img class="plain" src="figures/dev_board.png" />

- Development kits are very affordable
- You can reprogram the logic as many times as you want, which means you can
  iterate on your designs. Just like traditional software!

---

A simple OR gate

```tut:silent
import spinal.core._

class OrGate extends Component {
  val io = new Bundle {
    val a = in Bool
    val b = in Bool

    val output = out Bool
  }

  io.output := io.a | io.b
}
```
---

## Testing our components

- We can test our hardware description without the need for real hardware!
- This enables a cycle time closer to what software developers are confortable
  with

---

Test the OR gate

```tut:silent
import spinal.core.sim._

def testOrGate = {
  SimConfig.withWave.compile(new OrGate).doSim { dut =>
    val truthTable = List(
      (false, false, false),
      (true, false, true),
      (false, true, true),
      (true, true, true)
    )

    for ((a, b, expected_output) <- truthTable) {
      println(s"$a & $b => $expected_output")
      dut.io.a #= a
      dut.io.b #= b

      // Sleep one virtual cycle for the signal to propagate
      sleep(1)
      assert(dut.io.output.toBoolean == expected_output)
    }
  }
}
```

---

Run the test

```tut
testOrGate
```

---

## Sequential Logic

- The next state of a sequential logic network depends on its previous state
- Synchronous logic is logic that only updates its state on the edge of a clock

---

Count up to `maxValue`, then reset back to 0

```tut:silent
object Counter {
  def requiredBitWidth(value: Int): Int = {
    (31 to 0 by -1)
      .find({ index =>
        (value & (1 << index)) != 0
      })
      .map(_ + 1)
      .getOrElse(0)
  }
}

class Counter(maxValue: Int) extends Component {
  private val width = Counter.requiredBitWidth(maxValue)
  val io = new Bundle {
    val reset = in Bool
    val value = out UInt (width bits)
  }

  private val register = Reg(UInt(width bits))

  when(register === maxValue || io.reset) {
    register := 0
  }.otherwise {
    register := register + 1
  }
  io.value := register
}
```

---

And to test the counter

```tut:silent
def testCounter = {
SimConfig.withWave.compile(new Counter(42)).doSim { dut =>
  val expectedValues = (0 to 42) ++ (0 to 42)

  // Clock setup
  dut.clockDomain.forkStimulus(period = 10)
  dut.clockDomain.waitSampling();
  dut.clockDomain.waitFallingEdge();

  // Reset the register and wait one period
  dut.io.reset #= true
  dut.clockDomain.waitFallingEdge();
  dut.clockDomain.waitFallingEdge();

  // Sample the value and see that it is reset
  assert(dut.io.value.toInt == 0)
  dut.io.reset #= false

  // Watch it count up each period
  for (expectedValue <- expectedValues) {
    val value = dut.io.value.toInt
    assert(value == expectedValue)
    dut.clockDomain.waitFallingEdge()
  }
}
}
```

---

And then run the tests

```tut
testCounter
```

---

If we're having trouble understanding why something has gone wrong, we can look
at the signals using GtkWave

<img class="plain" src="figures/waveform.png" />

---

## Running on real hardware

<img class="plain" src="figures/just_like_the_simulations.jpg" />

---

Creating the "top level" module

```tut:silent
class Toplevel extends Component {
  // Based on the DE10-Lite
  val io = new Bundle {
    val KEY0 = in Bool
    val KEY1 = in Bool
    val LEDR = out UInt(10 bits)
  }

  // We need specific signal names here
  noIoPrefix()

  // Setup the clock for the counter
  val coreClockDomain = new ClockDomain(
    clock = io.KEY0,
    config = ClockDomainConfig(
      clockEdge        = FALLING,
    )
  )

  // Create our counter
  val counterArea = new ClockingArea(coreClockDomain) {
      private val counter = new Counter(10)

      // Tie the counter to the toplevel names
      io.LEDR <> counter.io.value.resize(10 bits)
      counter.io.reset := !io.KEY1
  }
}

object Toplevel {
  def main(args: Array[String]) {
    SpinalVerilog(new Toplevel)
  }
}
```

---

When we run `main` from `Toplevel` we get:

```verilog
// Generator : SpinalHDL v1.3.6    git head : 10854057c32ae371aabc9a340c367e9bbc159fcd
// Date      : 28/07/2019, 14:14:19
// Component : Toplevel


module Counter (
      input   io_reset,
      output [3:0] io_value,
      input   KEY0);
  reg [3:0] register_1_;
  assign io_value = register_1_;
  always @ (negedge KEY0) begin
    if(((register_1_ == (4'b1010)) || io_reset))begin
      register_1_ <= (4'b0000);
    end else begin
      register_1_ <= (register_1_ + (4'b0001));
    end
  end

endmodule

module Toplevel (
      input   KEY0,
      input   KEY1,
      output [9:0] LEDR);
  wire  _zz_1_;
  wire [3:0] counterArea_counter_io_value;
  Counter counterArea_counter (
    .io_reset(_zz_1_),
    .io_value(counterArea_counter_io_value),
    .KEY0(KEY0)
  );
  assign LEDR = {6'd0, counterArea_counter_io_value};
  assign _zz_1_ = (! KEY1);
endmodule
```

---

## Building

- Run that file through whatever Kafka-esque build system your FPGA
  manufacturer gives you
- At this step, things like timing analysis will be done
- Then we load it on the board and cross our fingers that it works!

---

<video controls>
<source src="figures/running_board.mp4" type="video/mp4" />
</video>

---

## More than just toys

- VexRiscV is a highly configurable, full featured RISC-V based soft CPU
- https://github.com/SpinalHDL/VexRiscv
- The core library ships with many common components, like counters buses and
  video controllers

---

## Other useful features

- SpinalHDL can automatically detect when you've crossed clock domains
- Generated VHDL / Verilog can integrate easily with existing IP

---

## Caveats

- Documentation is a little weak
- Some questionable development practices
- Mostly a result of not too many people using it

---

## Other fancy HDLs

---

## Clash

- A high-level sythensis compiler for Haskell
- Write your code as regular Haskell and compile it to VHDL or Verilog
- Some performance loss when optimizer isn't good enough
- The generated HDL is very hard to follow

---

```haskell
import Clash.Prelude

countUpTo :: (Num a, Ord a) => a -> a -> a
countUpTo max acc
  | acc < max = acc + 1
  | otherwise = 0

counterTransition :: (Num a, Ord a) => a -> a -> b -> (a, a)
counterTransition max currentState _ = (nextState, output)
  where
    nextState = countUpTo max currentState
    output = currentState

counter ::
     (HiddenClockResetEnable dom, Undefined a, Num a, Ord a)
  => a
  -> Signal dom b
  -> Signal dom a
counter max = mealy (counterTransition max) 0

topEntity ::
     Clock System
  -> Reset System
  -> Enable System
  -> Signal System ()
  -> Signal System (Unsigned 9)
topEntity = exposeClockResetEnable $ counter 42
```

---

## Chisel

- Another Scala DSL
- Very similar syntax to SpinalHDL
- Has a lot of contributors
- Very volatile development

---

## MyHDL

- A Python DSL based on function annotations
- Syntatically similar to Verilog
- Slow development

---

TODO
