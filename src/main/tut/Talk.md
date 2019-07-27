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

<img class="plain" src="figures/just_like_the_simulations.jpg" />

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

- The next state of a sequential logic network depends on on its previous state
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
  
    // Start the clock and wait for everything to settle
    dut.clockDomain.forkStimulus(period = 10)
    dut.clockDomain.waitSampling();
    dut.clockDomain.waitFallingEdge();
  
    // Reset our counter since it can start with any value
    dut.io.reset #= true
    // Our register updates on the rising edge of the clock,
    // so we send the reset signal right in the middle of
    // two update points
    dut.clockDomain.waitFallingEdge();
  
    // Assert that we've reset to 0, then clear the
    // reset signal
    assert(dut.io.value.toInt == 0)
    dut.clockDomain.waitFallingEdge();
    dut.io.reset #= false
  
    // Now watch our counter count up each cycle
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
