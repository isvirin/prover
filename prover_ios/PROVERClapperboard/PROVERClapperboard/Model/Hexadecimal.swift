import Foundation

struct Hexadecimal: Codable {
  
  // Class functions
  // Validate hex string
  private static func validate(_ text: String) -> Bool {
    
    let validCharacters = "0123456789abcdef"
    
    for char in text {
      if !validCharacters.contains(char) {
        return false
      }
    }
    
    return true
  }
  
  // Instance
  
  let prefix = "0x"
  let value: String
  var withPrefix: String {
    return prefix + value
  }
  
  // Initialization
  init?(_ text: String) {
    
    var text = text
    if text.hasPrefix(prefix) {
      text.removeFirst(2)
    }
    
    guard Hexadecimal.validate(text) else { return nil }
    
    value = text
  }
  
  init(_ number: Int) {
    self.init(String(number, radix: 16))!
  }
  
  // Int64 representation of hex number
  var toInt64: Int64? {
    return Int64(value, radix: 16)!
  }
  
  // [UInt8] representation of hex number
  var toBytes: [UInt8] {
    
    var hex = value
    var result = [UInt8]()
    
    while hex.count > 0 {
      let subString = hex.prefix(2)
      let byte = UInt8(subString, radix: 16)!
      result.append(byte)
      
      if hex.count > 1 {
        hex.removeFirst(2)
      } else {
        hex.removeAll()
      }
    }
    
    return result
  }
  
  // Big Endian representation of hex number
  func bigEndian(trailing: Int) -> [UInt8] {
  
    let significant = toBytes
    let zeroes = Array(repeatElement(UInt8(0), count: trailing - significant.count))
  
    return zeroes + significant
  }
}
