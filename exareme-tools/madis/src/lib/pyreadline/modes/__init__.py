__all__ = ["emacs", "notemacs", "vi"]
import emacs
import notemacs
import vi

editingmodes = [emacs.EmacsMode, notemacs.NotEmacsMode, vi.ViMode]

# add check to ensure all modes have unique mode names
