import { memo } from 'react';
import type { MenuItem } from '@/lib/types';

interface ControlMenuProps {
  items: MenuItem[];
}

function ControlMenu({ items }: ControlMenuProps) {
  return (
    <div className="space-y-4" data-testid="scan-results">
      {items.map((item) => (
        <div
          key={item.id}
          className="bg-card border border-border rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow"
          data-testid="menu-item"
        >
          <h3 className="text-base font-bold text-foreground mb-1">
            {item.originalName}
          </h3>
          <p className="text-sm text-foreground mb-3">
            {item.translatedName}
          </p>
          <div className="flex items-baseline gap-2">
            <span className="text-sm font-semibold text-primary">
              {item.priceInfo.originalFormatted}
            </span>
            <span className="text-sm text-muted-foreground">
              ({item.priceInfo.convertedFormatted})
            </span>
          </div>
        </div>
      ))}
    </div>
  );
}

export default memo(ControlMenu);
