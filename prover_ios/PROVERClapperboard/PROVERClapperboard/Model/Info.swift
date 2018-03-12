import Foundation

struct Info {
  
  let nonce: Hexadecimal
  let contractAddress: Hexadecimal
  let gasPrice: Hexadecimal
  let ethBalance: Hexadecimal
  
  init?(from data: InfoReslut) {
    
    guard let nonceString = data.nonce,
      let ethBalanceString = data.ethBalance else {
        return nil
    }
    
    guard let nonce = Hexadecimal(nonceString),
      let contractAddress = Hexadecimal(data.contractAddress),
      let gasPrice = Hexadecimal(data.gasPrice),
      let ethBalance = Hexadecimal(ethBalanceString) else {
        return nil
    }
    
    self.nonce = nonce
    self.contractAddress = contractAddress
    self.gasPrice = gasPrice
    self.ethBalance = ethBalance
  }
}
