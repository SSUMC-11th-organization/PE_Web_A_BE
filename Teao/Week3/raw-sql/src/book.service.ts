import { Injectable } from '@nestjs/common';
import { BookRepository } from './book.repository';

@Injectable()
export class BookService {
  constructor(private readonly bookRepository: BookRepository) {}

  getAllBooks() {
    return this.bookRepository.findAll();
  }

  getBooksByCategory(categoryId: number) {
    return this.bookRepository.findByCategory(categoryId);
  }

  async createBook(body: {
    categoryId: number;
    title: string;
    description?: string;
    author?: string;
  }) {
    await this.bookRepository.create(body);
    return '도서 등록이 완료되었습니다!';
  }
}
