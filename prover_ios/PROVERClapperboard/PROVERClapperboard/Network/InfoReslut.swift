import Foundation

struct InfoReslut: Decodable {

  let nonce: String?
  let contractAddress: String
  let gasPrice: String
  let ethBalance: String?
}
