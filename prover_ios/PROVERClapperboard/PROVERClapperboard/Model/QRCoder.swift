import Foundation
import UIKit

struct QRCoder {
  
  private func convert(cmage: CIImage) -> UIImage {
    let context: CIContext = CIContext.init(options: nil)
    let cgImage: CGImage = context.createCGImage(cmage, from: cmage.extent)!
    let image: UIImage = UIImage.init(cgImage: cgImage)
    return image
  }
  
  func encode(_ block: [UInt8]) -> UIImage {
    
    let data = Data(block)
    
    let filter = CIFilter(name: "CIQRCodeGenerator")!
    filter.setValue(data, forKey: "inputMessage")
    
    filter.setValue("H", forKey: "inputCorrectionLevel")
    let transform = CGAffineTransform(scaleX: 5, y: 5)
    
    let image = convert(cmage: filter.outputImage!.transformed(by: transform))
    
    return image
  }
}
