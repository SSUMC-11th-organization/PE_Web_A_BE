import { Injectable } from '@nestjs/common';
import { RentalRepository } from './rental.repository';

@Injectable()
export class RentalService {
  constructor(private readonly rentalRepository: RentalRepository) {}

  async createRental(body: { userId: number; bookId: number }) {
    await this.rentalRepository.create(body.userId, body.bookId);
    return '대여 기록이 생성되었습니다!';
  }
}
