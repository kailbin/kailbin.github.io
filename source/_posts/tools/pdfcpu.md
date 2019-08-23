---
title: pdfcpu 操作 pdf 文件
date: 2019-08-23
tags: [Tool]
categories:
  - Tools
id: pdfcpu
---

## 常用命令

- **截取指定的 Page** `./pdfcpu trim -pages '2-4' in.pdf out.pdf`
- **删除 Page** `./pdfcpu pages remove -pages 1 in.pdf out.pdf`
- 优化压缩PDF `./pdfcpu optimize in.pdf out.pdf`
- 合并文件： `./pdfcpu merge out.pdf in1.pdf in2.pdf in3.pdf`

<!-- more -->

## 页面选择器

> https://pdfcpu.io/getting_started/page_selection
>
> `#` 代表页码，多个选择器逗号分割

| expression | page selection       |
| :--------- | :------------------- |
| even       | 选择偶数页           |
| odd        | 选择奇数页           |
| #          | 指定的页码           |
| #-#        | 包含起始结束页       |
| !#         | 不包含当前页         |
| !#-#       | 不包含起始，包含结尾 |
| #-         | 当前页到最后         |
| -#         | 第一页到当前页       |
| !-#        | 第二页到最后页       |



## 其他功能

> https://pdfcpu.io/about/command_set

- Read (builds xref table from PDF file)
- Validate (validates PDF files up to version 7.0)
- Write (writes xref table to PDF file)
- Optimize (gets rid of redundancies like duplicate fonts, images)
- Merge (a set of PDF files into one consolidated PDF file)
- Split (split multi-page PDF into several PDFs according to split span)
- Trim (generate a custom version of a PDF including selected pages)
- Rotate selected pages
- Stamp/Watermark selected pages with text, an image or a PDF page
- N-up (rearrange pages into grid layout for reduced number of pages)
- Grid (rearrange pages into grid layout for enhanced browsing)
- Import convert/import images into PDF
- Extract Images (extract embedded images of a PDF into a given dir)
- Extract Fonts (extract all embedded fonts of a PDF file into a given dir)
- Extract Content (extract the PDF-Source into given dir)
- Extract Pages (extract specific pages into a given dir)
- Extract Metadata (extract XML metadata)
- Manage (add,remove,list,extract) embedded file attachments
- Encrypt (sets password protection)
- Decrypt (removes password protection)
- Change user/owner password
- Manage (add,list) user access permissions
- Manage (insert, remove) pages