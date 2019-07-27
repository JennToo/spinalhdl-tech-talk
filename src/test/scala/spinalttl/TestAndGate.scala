package spinalttl

import spinal.core.sim._
import org.scalatest.FunSuite

class TestAndGate extends FunSuite {
  test("It's an AND gate...") {
    SimConfig.withWave.compile(new AndGate).doSim { dut =>
      val truthTable = List(
        (false, false, false),
        (true, false, false),
        (false, true, false),
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
}
