import Foundation

extension Data {
    var hexDescription: String {
        return reduce("") {$0 + String(format: "%02x", $1)}
    }
  
  var bytesDescription: [UInt8] {
    
    let array = self.withUnsafeBytes { (pointer: UnsafePointer<UInt8>) -> [UInt8] in
      let buffer = UnsafeBufferPointer(start: pointer, count: self.count)
      return [UInt8](buffer)
    }
    return array
  }
}
