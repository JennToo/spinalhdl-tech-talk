package spinalttl

import spinal.core.sim._
import org.scalatest.FunSuite

class TestCounter extends FunSuite {
  test("A counter should count to its max and then reset") {
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
}
