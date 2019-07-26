package spinalttl

import spinal.core._

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
