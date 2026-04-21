import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { TagService } from '../../../core/services/tag.service';
import { Tag } from '../../../core/models';

@Component({
  selector: 'app-tag-selector',
  templateUrl: './tag-selector.component.html',
  styleUrls: ['./tag-selector.component.css']
})
export class TagSelectorComponent implements OnInit {
  @Input() selectedTagIds: number[] = [];
  @Output() selectedTagIdsChange = new EventEmitter<number[]>();

  tags: Tag[] = [];
  isLoading = false;

  constructor(private tagService: TagService) {}

  ngOnInit(): void {
    this.loadTags();
  }

  loadTags(): void {
    this.isLoading = true;
    this.tagService.getAllTags().subscribe({
      next: (tags: Tag[]) => {
        this.tags = tags;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading tags:', error);
        this.isLoading = false;
      }
    });
  }

  toggleTag(tagId: number): void {
    const index = this.selectedTagIds.indexOf(tagId);
    if (index > -1) {
      this.selectedTagIds.splice(index, 1);
    } else {
      this.selectedTagIds.push(tagId);
    }
    this.selectedTagIdsChange.emit(this.selectedTagIds);
  }

  isTagSelected(tagId: number): boolean {
    return this.selectedTagIds.includes(tagId);
  }

  getSelectedTags(): Tag[] {
    return this.tags.filter(tag => this.selectedTagIds.includes(tag.id!));
  }

  get selectedTagsText(): string {
    return this.getSelectedTags().map(t => t.name).join(', ');
  }
}
