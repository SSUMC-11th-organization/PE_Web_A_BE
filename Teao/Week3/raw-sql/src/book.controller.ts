import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { BookService } from './book.service';

@Controller('books')
export class BookController {
  constructor(private readonly bookService: BookService) {}

  @Get()
  getBooks() {
    return this.bookService.getAllBooks();
  }

  @Get('category/:categoryId')
  getBooksByCategory(@Param('categoryId') categoryId: string) {
    return this.bookService.getBooksByCategory(Number(categoryId));
  }

  @Post()
  createBook(@Body() body: {
    categoryId: number;
    title: string;
    description?: string;
    author?: string;
  }) {
    return this.bookService.createBook(body);
  }
}
