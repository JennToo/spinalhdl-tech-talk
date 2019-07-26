package spinalttl

object TestCounter {
  import spinal.core.sim._

  def main(args: Array[String]) {
    SimConfig.withWave.compile(new Counter(42)).doSim { dut =>
      val expectedValues = (0 to 42) ++ (0 to 42)

      dut.io.reset #= true
      sleep(1)
      assert(dut.io.value.toInt == 0)
      dut.io.reset #= false

      for (expectedValue <- expectedValues) {
        val value = dut.io.value.toInt
        println(s"$value == $expectedValue")
        assert(value == expectedValue)
        sleep(2)
      }
    }
  }
}
