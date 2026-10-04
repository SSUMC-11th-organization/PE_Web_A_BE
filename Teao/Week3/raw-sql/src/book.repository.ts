import { Inject, Injectable } from '@nestjs/common';
import type { Pool, ResultSetHeader, RowDataPacket } from 'mysql2/promise';
import { DATABASE_CONNECTION } from './database.provider';

type CreateBookBody = {
  categoryId: number;
  title: string;
  description?: string;
  author?: string;
};

@Injectable()
export class BookRepository {
  constructor(
    @Inject(DATABASE_CONNECTION) private readonly pool: Pool,
  ) {}

  async findAll(): Promise<RowDataPacket[]> {
    const [rows] = await this.pool.query<RowDataPacket[]>('SELECT * FROM book');
    return rows;
  }

  async findByCategory(categoryId: number): Promise<RowDataPacket[]> {
    const sql = 'SELECT * FROM book WHERE category_id = ?';
    const [rows] = await this.pool.execute<RowDataPacket[]>(sql, [categoryId]);
    return rows;
  }

  async create(body: CreateBookBody): Promise<ResultSetHeader> {
    // 현재 1주차 book 테이블의 필수 author 컬럼을 고려해 기본값을 둡니다.
    const sql = `
      INSERT INTO book
        (category_id, title, description, author, total_copies, available_copies)
      VALUES (?, ?, ?, ?, 1, 1)
    `;
    const [result] = await this.pool.execute<ResultSetHeader>(sql, [
      body.categoryId,
      body.title,
      body.description ?? null,
      body.author ?? '미상',
    ]);
    return result;
  }
}
