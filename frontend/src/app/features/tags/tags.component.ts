import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TagService } from '../../core';
import { Tag, DEFAULT_TAG_COLORS } from '../../core/models';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-tags',
  templateUrl: './tags.component.html',
  styleUrls: ['./tags.component.css'],
  imports: [CommonModule, ReactiveFormsModule, FormsModule]
})
export class TagsComponent implements OnInit {
  tags: Tag[] = [];
  tagForm: FormGroup;
  isEditing = false;
  editingTagId: number | null = null;
  searchQuery = '';
  selectedColor = DEFAULT_TAG_COLORS[0];
  tagUsage: { [key: number]: { goalCount: number } } = {};
  showModal = false;

  availableColors = DEFAULT_TAG_COLORS;

  private toastService = inject(ToastService);

  constructor(
    private fb: FormBuilder,
    private tagService: TagService,
    private cdr: ChangeDetectorRef
  ) {
    this.tagForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      color: [DEFAULT_TAG_COLORS[0], Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadTags();
  }

  loadTags(): void {
    this.tagService.getAllTags().subscribe({
      next: (tags) => {
        this.tags = tags;
        this.cdr.detectChanges();
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to load tags';
        this.toastService.error(message, `Error ${error.status || ''}`);
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit(): void {
    if (this.tagForm.invalid) {
      return;
    }

    const tagData = this.tagForm.value;

    if (this.isEditing && this.editingTagId) {
      this.tagService.updateTag(this.editingTagId, tagData).subscribe({
        next: () => {
          this.toastService.success('Tag updated successfully!');
          this.resetForm();
          this.loadTags();
        },
        error: (error) => {
          const message = error.error?.message || error.error?.error || error.message || 'Failed to update tag';
          this.toastService.error(message, `Error ${error.status || ''}`);
        }
      });
    } else {
      this.tagService.createTag(tagData).subscribe({
        next: () => {
          this.toastService.success('Tag created successfully!');
          this.resetForm();
          this.loadTags();
        },
        error: (error) => {
          const message = error.error?.message || error.error?.error || error.message || 'Failed to create tag';
          this.toastService.error(message, `Error ${error.status || ''}`);
        }
      });
    }
  }

  editTag(tag: Tag): void {
    this.isEditing = true;
    this.editingTagId = tag.id!;
    this.selectedColor = tag.color;
    this.tagForm.patchValue({
      name: tag.name,
      description: tag.description,
      color: tag.color
    });
    this.openModal();
  }

  deleteTag(id: number): void {
    if (confirm('Are you sure you want to delete this tag?')) {
      this.tagService.deleteTag(id).subscribe({
        next: () => {
          this.toastService.success('Tag deleted successfully!');
          this.loadTags();
        },
        error: (error) => {
          const message = error.error?.message || error.error?.error || error.message || 'Failed to delete tag';
          this.toastService.error(message, `Error ${error.status || ''}`);
        }
      });
    }
  }

  searchTags(): void {
    if (this.searchQuery.trim()) {
      this.tagService.searchTags(this.searchQuery).subscribe({
        next: (tags) => {
          this.tags = tags;
        },
        error: (error) => {
          const message = error.error?.message || error.error?.error || error.message || 'Failed to search tags';
          this.toastService.error(message, `Error ${error.status || ''}`);
        }
      });
    } else {
      this.loadTags();
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadTags();
  }

  resetForm(): void {
    this.isEditing = false;
    this.editingTagId = null;
    this.selectedColor = DEFAULT_TAG_COLORS[0];
    this.tagForm.reset({
      color: DEFAULT_TAG_COLORS[0]
    });
  }

  openModal(): void {
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.resetForm();
  }

  selectColor(color: string): void {
    this.selectedColor = color;
    this.tagForm.patchValue({ color });
  }

  getUsageCount(tagId: number): number {
    const usage = this.tagUsage[tagId];
    return usage ? usage.goalCount : 0;
  }

  getUsageText(tagId: number): string {
    const usage = this.tagUsage[tagId];
    if (!usage) return 'Not used';
    const count = usage.goalCount;
    return count > 0 ? `Used in ${count} task${count > 1 ? 's' : ''}` : 'Not used';
  }

  getFilteredTags(): Tag[] {
    if (!this.searchQuery.trim()) {
      return this.tags;
    }
    return this.tags.filter(tag =>
      tag.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      tag.description.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

}
