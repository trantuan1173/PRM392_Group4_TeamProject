package com.example.prm392_group4_teamproject;

public class Pagination {
    public int page;
    public int limit;
    public int total;
    public int pages;

    public Pagination(int page, int limit, int total, int pages) {
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.pages = pages;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
