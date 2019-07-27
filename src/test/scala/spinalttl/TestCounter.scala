package spinalttl

import spinal.core.sim._
import org.scalatest.FunSuite

class TestCounter extends FunSuite {
  test("A counter should count to its max and then reset") {
    SimConfig.withWave.compile(new Counter(42)).doSim { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      val expectedValues = (0 to 42) ++ (0 to 42)

      dut.clockDomain.waitSampling();

      dut.io.reset #= true
      dut.clockDomain.waitFallingEdge();

      assert(dut.io.value.toInt == 0)
      dut.clockDomain.waitFallingEdge();
      dut.io.reset #= false

      for (expectedValue <- expectedValues) {
        val value = dut.io.value.toInt
        assert(value == expectedValue)
        dut.clockDomain.waitFallingEdge()
      }
    }
  }
}
