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

Testing our OR gate

```tut:book
import spinal.core.sim._

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
```
