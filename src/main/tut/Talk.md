## What is SpinalHDL?

- A hardware description language (HDL)
- Used to program the internal logic layout of an FPGA (and ASICs too, but those are expensive!)
- Similar to VHDL and Verilog in functionality, but is based on Scala

---

## Whirlwind intro to FPGAs

<img class="plain" src="figures/fpga.jpg" />

- An FPGA is a collection of programmable logic elements
- You can arrange the logic elements to behave like discrete logic components
  wired together, or like the transistors in an ASIC

---

## Whirlwind intro to FPGAs

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
