import { newMockEvent } from "matchstick-as"
import { ethereum, Address, BigInt, Bytes } from "@graphprotocol/graph-ts"
import {
  OwnershipTransferred,
  QuotaUpgraded,
  RentPriceUpdated,
  TicketDestroyed,
  TicketRentedWithTBA
} from "../generated/ligher/ligher"

export function createOwnershipTransferredEvent(
  previousOwner: Address,
  newOwner: Address
): OwnershipTransferred {
  let ownershipTransferredEvent =
    changetype<OwnershipTransferred>(newMockEvent())

  ownershipTransferredEvent.parameters = new Array()

  ownershipTransferredEvent.parameters.push(
    new ethereum.EventParam(
      "previousOwner",
      ethereum.Value.fromAddress(previousOwner)
    )
  )
  ownershipTransferredEvent.parameters.push(
    new ethereum.EventParam("newOwner", ethereum.Value.fromAddress(newOwner))
  )

  return ownershipTransferredEvent
}

export function createQuotaUpgradedEvent(
  renter: Address,
  tokenId: BigInt,
  tbaAddress: Address,
  hexNostrPubKey: string,
  rent: BigInt
): QuotaUpgraded {
  let quotaUpgradedEvent = changetype<QuotaUpgraded>(newMockEvent())

  quotaUpgradedEvent.parameters = new Array()

  quotaUpgradedEvent.parameters.push(
    new ethereum.EventParam("renter", ethereum.Value.fromAddress(renter))
  )
  quotaUpgradedEvent.parameters.push(
    new ethereum.EventParam(
      "tokenId",
      ethereum.Value.fromUnsignedBigInt(tokenId)
    )
  )
  quotaUpgradedEvent.parameters.push(
    new ethereum.EventParam(
      "tbaAddress",
      ethereum.Value.fromAddress(tbaAddress)
    )
  )
  quotaUpgradedEvent.parameters.push(
    new ethereum.EventParam(
      "hexNostrPubKey",
      ethereum.Value.fromString(hexNostrPubKey)
    )
  )
  quotaUpgradedEvent.parameters.push(
    new ethereum.EventParam("rent", ethereum.Value.fromUnsignedBigInt(rent))
  )

  return quotaUpgradedEvent
}

export function createRentPriceUpdatedEvent(
  oldPrice: BigInt,
  newPrice: BigInt
): RentPriceUpdated {
  let rentPriceUpdatedEvent = changetype<RentPriceUpdated>(newMockEvent())

  rentPriceUpdatedEvent.parameters = new Array()

  rentPriceUpdatedEvent.parameters.push(
    new ethereum.EventParam(
      "oldPrice",
      ethereum.Value.fromUnsignedBigInt(oldPrice)
    )
  )
  rentPriceUpdatedEvent.parameters.push(
    new ethereum.EventParam(
      "newPrice",
      ethereum.Value.fromUnsignedBigInt(newPrice)
    )
  )

  return rentPriceUpdatedEvent
}

export function createTicketDestroyedEvent(
  recipient: Address,
  tokenId: BigInt,
  tbaAddress: Address,
  hexNostrPubKey: string,
  amount: BigInt
): TicketDestroyed {
  let ticketDestroyedEvent = changetype<TicketDestroyed>(newMockEvent())

  ticketDestroyedEvent.parameters = new Array()

  ticketDestroyedEvent.parameters.push(
    new ethereum.EventParam("recipient", ethereum.Value.fromAddress(recipient))
  )
  ticketDestroyedEvent.parameters.push(
    new ethereum.EventParam(
      "tokenId",
      ethereum.Value.fromUnsignedBigInt(tokenId)
    )
  )
  ticketDestroyedEvent.parameters.push(
    new ethereum.EventParam(
      "tbaAddress",
      ethereum.Value.fromAddress(tbaAddress)
    )
  )
  ticketDestroyedEvent.parameters.push(
    new ethereum.EventParam(
      "hexNostrPubKey",
      ethereum.Value.fromString(hexNostrPubKey)
    )
  )
  ticketDestroyedEvent.parameters.push(
    new ethereum.EventParam("amount", ethereum.Value.fromUnsignedBigInt(amount))
  )

  return ticketDestroyedEvent
}

export function createTicketRentedWithTBAEvent(
  renter: Address,
  tokenId: BigInt,
  tbaAddress: Address,
  nostrPubKey: Bytes,
  rent: BigInt
): TicketRentedWithTBA {
  let ticketRentedWithTbaEvent = changetype<TicketRentedWithTBA>(newMockEvent())

  ticketRentedWithTbaEvent.parameters = new Array()

  ticketRentedWithTbaEvent.parameters.push(
    new ethereum.EventParam("renter", ethereum.Value.fromAddress(renter))
  )
  ticketRentedWithTbaEvent.parameters.push(
    new ethereum.EventParam(
      "tokenId",
      ethereum.Value.fromUnsignedBigInt(tokenId)
    )
  )
  ticketRentedWithTbaEvent.parameters.push(
    new ethereum.EventParam(
      "tbaAddress",
      ethereum.Value.fromAddress(tbaAddress)
    )
  )
  ticketRentedWithTbaEvent.parameters.push(
    new ethereum.EventParam(
      "nostrPubKey",
      ethereum.Value.fromFixedBytes(nostrPubKey)
    )
  )
  ticketRentedWithTbaEvent.parameters.push(
    new ethereum.EventParam("rent", ethereum.Value.fromUnsignedBigInt(rent))
  )

  return ticketRentedWithTbaEvent
}
