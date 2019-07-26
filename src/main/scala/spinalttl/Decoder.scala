package spinalttl

import spinal.core._

class Decoder(input_width: Int) extends Component {
  val io = new Bundle {
    val input = in Bits (input_width bits)
    val output = out Bits ((1 << input_width) bits)
  }

  io.output := B(IntToUInt(1) << io.input.asUInt)
}

object Decoder {
  def main(args: Array[String]) {
    SpinalVhdl(new Decoder(6))
  }
}
