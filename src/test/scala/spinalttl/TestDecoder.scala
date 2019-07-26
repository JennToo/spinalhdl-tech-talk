package spinalttl
import spinal.core._

object TestDecoder {
  import spinal.core.sim._

  def main(args: Array[String]) {
    SimConfig.withWave.compile(new Decoder(5)).doSim { dut =>
      val decodeCases = List(
        (0, 0x01),
        (5, 0x20),
        (10, 0x400)
      )

      for ((input, output) <- decodeCases) {
        println(s"$input => $output")
        dut.io.input #= input
        sleep(1)
        assert(dut.io.output.toLong == output)
      }
    }
  }
}
