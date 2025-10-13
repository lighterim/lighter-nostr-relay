import {
  OwnershipTransferred as OwnershipTransferredEvent,
  QuotaUpgraded as QuotaUpgradedEvent,
  RentPriceUpdated as RentPriceUpdatedEvent,
  TicketDestroyed as TicketDestroyedEvent,
  TicketRentedWithTBA as TicketRentedWithTBAEvent
} from "../generated/ligher/ligher"
import {
  OwnershipTransferred,
  QuotaUpgraded,
  RentPriceUpdated,
  TicketDestroyed,
  TicketRentedWithTBA
} from "../generated/schema"

export function handleOwnershipTransferred(
  event: OwnershipTransferredEvent
): void {
  let entity = new OwnershipTransferred(
    event.transaction.hash.concatI32(event.logIndex.toI32())
  )
  entity.previousOwner = event.params.previousOwner
  entity.newOwner = event.params.newOwner

  entity.blockNumber = event.block.number
  entity.blockTimestamp = event.block.timestamp
  entity.transactionHash = event.transaction.hash

  entity.save()
}

export function handleQuotaUpgraded(event: QuotaUpgradedEvent): void {
  let entity = new QuotaUpgraded(
    event.transaction.hash.concatI32(event.logIndex.toI32())
  )
  entity.renter = event.params.renter
  entity.tokenId = event.params.tokenId
  entity.tbaAddress = event.params.tbaAddress
  entity.hexNostrPubKey = event.params.hexNostrPubKey
  entity.rent = event.params.rent

  entity.blockNumber = event.block.number
  entity.blockTimestamp = event.block.timestamp
  entity.transactionHash = event.transaction.hash

  entity.save()
}

export function handleRentPriceUpdated(event: RentPriceUpdatedEvent): void {
  let entity = new RentPriceUpdated(
    event.transaction.hash.concatI32(event.logIndex.toI32())
  )
  entity.oldPrice = event.params.oldPrice
  entity.newPrice = event.params.newPrice

  entity.blockNumber = event.block.number
  entity.blockTimestamp = event.block.timestamp
  entity.transactionHash = event.transaction.hash

  entity.save()
}

export function handleTicketDestroyed(event: TicketDestroyedEvent): void {
  let entity = new TicketDestroyed(
    event.transaction.hash.concatI32(event.logIndex.toI32())
  )
  entity.recipient = event.params.recipient
  entity.tokenId = event.params.tokenId
  entity.tbaAddress = event.params.tbaAddress
  entity.hexNostrPubKey = event.params.hexNostrPubKey
  entity.amount = event.params.amount

  entity.blockNumber = event.block.number
  entity.blockTimestamp = event.block.timestamp
  entity.transactionHash = event.transaction.hash

  entity.save()
}

export function handleTicketRentedWithTBA(
  event: TicketRentedWithTBAEvent
): void {
  let entity = new TicketRentedWithTBA(
    event.transaction.hash.concatI32(event.logIndex.toI32())
  )
  entity.renter = event.params.renter
  entity.tokenId = event.params.tokenId
  entity.tbaAddress = event.params.tbaAddress
  entity.nostrPubKey = event.params.nostrPubKey
  entity.rent = event.params.rent

  entity.blockNumber = event.block.number
  entity.blockTimestamp = event.block.timestamp
  entity.transactionHash = event.transaction.hash

  entity.save()
}
